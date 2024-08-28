/*
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 */
package com.ericsson.am.shared.vnfd.service;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_NODES_NFV_VNF_TYPE;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.am.shared.vnfd.model.servicemodel.ServiceModel;
import com.ericsson.am.shared.vnfd.model.typedefinition.TypeDefinitions;
import com.ericsson.am.shared.vnfd.service.exception.ToscaoException;

@Service
public class ToscaoServiceImpl implements ToscaoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaoServiceImpl.class);

    public static final String DESCRIPTOR_ID = "descriptor_id";
    public static final String SERVICE_MODEL_NAME_PREFIX = "EVNFM_PACKAGE_";

    public static final String SERVICE_MODEL_ID_EMPTY_ERROR_MESSAGE = "The service model ID is empty.";
    public static final String ERROR_MESSAGE_QUERYING_ALL_SERVICE_MODELS
            = "An exception occurred querying all service models: %s";
    public static final String DESCRIPTOR_ID_EMPTY_ERROR_MESSAGE = "The descriptor ID is empty.";
    public static final String ERROR_MESSAGE_QUERYING_SERVICE_MODEL
            = "An exception occurred when querying service model by id: %s";
    public static final String ERROR_MESSAGE_QUERYING_TYPE_DEFINITIONS
            = "An exception occurred when querying type definitions by id: %s";
    public static final String ERROR_MESSAGE_DELETING_SERVICE_MODEL
            = "An exception occurred deleting the package for service model id: %s %s";
    public static final String ERROR_MESSAGE_UPLOADING_CSAR
            = "An exception occurred while uploading the package to TOSCA-O: %s";
    public static final String ERROR_MESSAGE_CSAR_PATH_IS_NULL = "Path to package can't be null";

    private static final String SERVICE_MODEL_LOG = "Returned response from TOSCA-O {}";

    private static final String HEALTH_CHECK_URL = "/toscao/api/v2.4/plugins";
    private static final String SERVICE_MODELS_URL = "/toscao/api/%s/service-models";
    private static final String SERVICE_MODEL_URL = "/toscao/api/%s/service-models/%s";
    private static final String TYPE_DEFINITIONS_URL = "/toscao/api/%s/service-models/%s/type-definitions";

    @Value("${toscao.host}")
    private String toscaoHost;

    @Value("${toscao.port}")
    private String toscaoPort;

    @Value("${toscao.api.version}")
    private String toscaoApiVersion;

    @Autowired
    @Qualifier("toscaRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("retryToscaTemplate")
    private RetryTemplate retryToscaTemplate;

    @Override
    public Optional<List<ServiceModel>> getServiceModels() throws ToscaoException {
        LOGGER.info("Getting all service models");
        String url = buildHttpUrl(toscaoHost, toscaoPort, String.format(SERVICE_MODELS_URL, toscaoApiVersion));
        try {
            ParameterizedTypeReference<List<ServiceModel>> listServiceModelType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<List<ServiceModel>> allServiceModel = retryToscaTemplate.execute(retryContext -> restTemplate
                    .exchange(url, HttpMethod.GET, null, listServiceModelType));
            if (allServiceModel.getStatusCode().is2xxSuccessful() && allServiceModel.getBody() != null) {
                return Optional.of(allServiceModel.getBody());
            } else {
                LOGGER.info(SERVICE_MODEL_LOG, allServiceModel);
                return Optional.empty();
            }
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            } else {
                String message = String.format(ERROR_MESSAGE_QUERYING_ALL_SERVICE_MODELS, e.getMessage());
                LOGGER.error(message, e);
                throw new ToscaoException(message, e);
            }
        }
    }

    @Override
    public Optional<ServiceModel> getServiceModelByDescriptorId(String descriptorId) throws ToscaoException {
        if (StringUtils.isEmpty(descriptorId)) {
            throw new IllegalArgumentException(DESCRIPTOR_ID_EMPTY_ERROR_MESSAGE);
        } else {
            Optional<List<ServiceModel>> allServiceModel = getServiceModels();
            if (allServiceModel.isPresent()) {
                return getServiceModelByDescriptorId(descriptorId, allServiceModel.get());
            }
        }
        return Optional.empty();
    }

    private static Optional<ServiceModel> getServiceModelByDescriptorId(String descriptorId,
                                                                        List<ServiceModel> allServiceModel) {
        for (final ServiceModel serviceModel : allServiceModel) {
            if (serviceModel.getTopology() != null && serviceModel.getTopology().getNodeTemplates() != null &&
                    serviceModel.getTopology()
                            .getNodeTemplates()
                            .stream()
                            .filter(nodeTemplate -> TOSCA_NODES_NFV_VNF_TYPE.equals(nodeTemplate.getParentType()))
                            .flatMap(nodeTemplate -> nodeTemplate.getNodeProperties().stream())
                            .filter(nodeProperty -> DESCRIPTOR_ID.equals(nodeProperty.getName()))
                            .map(nodeProperty -> nodeProperty.getValue().toString())
                            .anyMatch(matchedValue -> matchedValue.equals(descriptorId))) {

                return Optional.of(serviceModel);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<TypeDefinitions> getTypeDefinitions(String serviceModelID) throws ToscaoException {
        try {
            String url = getTypeDefinitionUri(serviceModelID);
            ResponseEntity<TypeDefinitions> typeDefinition = retryToscaTemplate.execute(retryContext -> restTemplate.exchange(url,
                                                                                                                              HttpMethod.GET,
                                                                                                                              null,
                                                                                                                              TypeDefinitions.class));
            if (typeDefinition.getStatusCode().is2xxSuccessful() && typeDefinition.getBody() != null) {
                return Optional.of(typeDefinition.getBody());
            } else {
                LOGGER.info(SERVICE_MODEL_LOG, typeDefinition);
                return Optional.empty();
            }
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            } else {
                String message = String.format(ERROR_MESSAGE_QUERYING_TYPE_DEFINITIONS, e.getMessage());
                LOGGER.error(message, e);
                throw new ToscaoException(message, e);
            }
        }
    }

    @Override
    public Optional<ServiceModel> uploadPackageToToscao(Path pathToFile) throws ToscaoException {
        if (pathToFile == null) {
            throw new IllegalArgumentException(ERROR_MESSAGE_CSAR_PATH_IS_NULL);
        }
        String serviceModelName = SERVICE_MODEL_NAME_PREFIX + UUID.randomUUID();
        String url = buildHttpUrl(toscaoHost, toscaoPort, String.format(SERVICE_MODELS_URL, toscaoApiVersion));
        try {
            FileSystemResource fileSystemResource = new FileSystemResource(pathToFile.toFile());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileSystemResource);
            body.add("name", serviceModelName);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);

            ResponseEntity<ServiceModel> uploadPackage = retryToscaTemplate.execute(retryContext -> {
                LOGGER.info("Uploading Service Model. Attempt: {}", retryContext.getRetryCount());
                return restTemplate.exchange(url, HttpMethod.POST,
                                             requestEntity,
                                             ServiceModel.class);
            });

            if (uploadPackage.getStatusCode().is2xxSuccessful() && uploadPackage.getBody() != null) {
                return Optional.of(uploadPackage.getBody());
            } else {
                LOGGER.info(SERVICE_MODEL_LOG, uploadPackage);
                return Optional.empty();
            }
        } catch (HttpStatusCodeException e) {
            String message = String.format(ERROR_MESSAGE_UPLOADING_CSAR, e.getMessage());
            LOGGER.error(message, e);
            throw new ToscaoException(message, e);
        }
    }

    @Override
    public boolean deleteServiceModel(String serviceModelId) throws ToscaoException {
        if (StringUtils.isEmpty(serviceModelId)) {
            throw new IllegalArgumentException(SERVICE_MODEL_ID_EMPTY_ERROR_MESSAGE);
        }
        String uri = String.format(SERVICE_MODEL_URL, toscaoApiVersion, serviceModelId);
        String url = buildHttpUrl(toscaoHost, toscaoPort, uri);

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.TEXT_PLAIN);
            HttpEntity<Object> request = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> responseEntity = retryToscaTemplate.execute(retryContext -> {
                LOGGER.info("Deleting Service Model. Attempt: {}", retryContext.getRetryCount());
                return restTemplate.exchange(url, HttpMethod.DELETE,
                                             request,
                                             String.class);
            });
            LOGGER.debug("Delete service model response: {}", responseEntity);
            return responseEntity.getStatusCode().is2xxSuccessful() ||
                    HttpStatus.NOT_FOUND.equals(responseEntity.getStatusCode());
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return true;
            } else {
                String message = String.format(ERROR_MESSAGE_DELETING_SERVICE_MODEL, serviceModelId, e.getMessage());
                LOGGER.error(message, e);
                throw new ToscaoException(message, e);
            }
        }
    }

    private String getTypeDefinitionUri(String serviceModelID) {
        if (StringUtils.isEmpty(serviceModelID)) {
            throw new IllegalArgumentException(SERVICE_MODEL_ID_EMPTY_ERROR_MESSAGE);
        }
        return buildHttpUrl(
                toscaoHost,
                toscaoPort,
                String.format(TYPE_DEFINITIONS_URL, toscaoApiVersion, serviceModelID));
    }

    @Override
    public Optional<ServiceModel> getServiceModelByServiceModelId(String serviceModelId) throws ToscaoException {
        if (Strings.isBlank(serviceModelId)) {
            throw new IllegalArgumentException(SERVICE_MODEL_ID_EMPTY_ERROR_MESSAGE);
        }
        final String uri = String.format(SERVICE_MODEL_URL, toscaoApiVersion, serviceModelId);
        try {
            String url = buildHttpUrl(toscaoHost, toscaoPort, uri);
            ResponseEntity<ServiceModel> serviceModel = retryToscaTemplate.execute(retryContext -> restTemplate
                    .exchange(url, HttpMethod.GET, null, ServiceModel.class));
            if (serviceModel.getStatusCode().is2xxSuccessful() && serviceModel.getBody() != null) {
                return Optional.of(serviceModel.getBody());
            } else {
                LOGGER.info(SERVICE_MODEL_LOG, serviceModel);
                return Optional.empty();
            }
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            } else {
                String message = String.format(ERROR_MESSAGE_QUERYING_SERVICE_MODEL, e.getMessage());
                LOGGER.error(message, e);
                throw new ToscaoException(message, e);
            }
        }
    }

    @Override
    public String healthStatus() {
        try {
            ResponseEntity<String> health = retryToscaTemplate.execute(retryContext -> restTemplate.getForEntity(
                    buildHttpUrl(toscaoHost, toscaoPort, HEALTH_CHECK_URL), String.class));
            if (health.getStatusCode().equals(HttpStatus.OK)) {
                return "{health : true}";
            } else {
                return "{health : false}";
            }
        } catch (Exception e) {
            LOGGER.error("Unable to check TOSCA-O health due to {}", e.getMessage());
            return "{health : false}";
        }
    }

    private static String buildHttpUrl(String host, String port, String uri) {
        return String.format("http://%s:%s%s", host, port, uri);
    }
}
