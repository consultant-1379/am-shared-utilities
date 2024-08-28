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

import com.ericsson.am.shared.vnfd.model.servicemodel.NodeProperty;
import com.ericsson.am.shared.vnfd.model.servicemodel.NodeTemplate;
import com.ericsson.am.shared.vnfd.model.servicemodel.ServiceModel;
import com.ericsson.am.shared.vnfd.model.typedefinition.TypeDefinitions;
import com.ericsson.am.shared.vnfd.service.exception.ToscaoException;
import com.ericsson.am.shared.vnfd.utils.Constants;
import com.ericsson.am.shared.vnfd.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.ericsson.am.shared.vnfd.service.ToscaoServiceImpl.DESCRIPTOR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ToscaoServiceImpl.class})
@TestPropertySource(
        properties = {
            "toscao.host = test",
            "toscao.port = test",
            "toscao.api.version = test",
        })
public class ToscaoServiceTest {

    private static final String ALL_SERVICE_MODEL_SAMPLE_RESPONSE = "servicemodel/service-model-list.json";
    private static final String SERVICE_MODEL_SAMPLE_RESPONSE = "servicemodel/service-model.json";
    private static final String TYPE_DEFINITIONS_SAMPLE_RESPONSE = "servicemodel/type-definitions.json";

    private static final String HEALTH_CHECK_URL = "http://test:test/toscao/api/v2.4/plugins";

    @Autowired
    private ToscaoService toscaoService;

    @MockBean
    @Qualifier("toscaRestTemplate")
    private RestTemplate restTemplate;

    @MockBean
    @Qualifier("retryToscaTemplate")
    private RetryTemplate retryToscaTemplate;

    @BeforeEach
    void init() {
        mockRetryTemplateResult();
    }

    @Test
    public void testGetServiceModels() throws Exception {
        List<ServiceModel> allServiceModel = getListOfServiceModel();
        ResponseEntity<List<ServiceModel>> allServiceModelResponse = new ResponseEntity<>(allServiceModel,
                HttpStatus.OK);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<ParameterizedTypeReference<List<ServiceModel>>>any()))
                .thenReturn(allServiceModelResponse);

        Optional<List<ServiceModel>> returnedServiceModel = toscaoService.getServiceModels();

        assertThat(returnedServiceModel).isPresent();
        assertThat(returnedServiceModel.get()).hasSize(allServiceModel.size());
    }

    @Test
    public void testGetServiceModelsWithNoOkFromToscao() throws Exception {
        List<ServiceModel> allServiceModel = getListOfServiceModel();
        ResponseEntity<List<ServiceModel>> allServiceModelResponse = new ResponseEntity<>(allServiceModel,
                HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<ParameterizedTypeReference<List<ServiceModel>>>any()))
                .thenReturn(allServiceModelResponse);

        Optional<List<ServiceModel>> returnedServiceModel = toscaoService.getServiceModels();

        assertThat(returnedServiceModel).isNotPresent();
    }

    @Test
    public void testGetServiceModelsWit4xxException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<ParameterizedTypeReference<List<ServiceModel>>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> toscaoService.getServiceModels())
                .isInstanceOf(ToscaoException.class)
                .hasMessage(String.format(
                        ToscaoServiceImpl.ERROR_MESSAGE_QUERYING_ALL_SERVICE_MODELS, HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetServiceModelsWit5xxException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<ParameterizedTypeReference<List<ServiceModel>>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> toscaoService.getServiceModels())
                .isInstanceOf(ToscaoException.class)
                .hasMessage(String.format(ToscaoServiceImpl.ERROR_MESSAGE_QUERYING_ALL_SERVICE_MODELS,
                        HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void testGetServiceModelsWitNotFoundException() throws Exception {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<ParameterizedTypeReference<List<ServiceModel>>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        Optional<List<ServiceModel>> returnedServiceModel = toscaoService.getServiceModels();
        assertThat(returnedServiceModel).isNotPresent();
    }

    @Test
    public void testGetServiceModelByDescriptorId() throws Exception {
        List<ServiceModel> allServiceModel = getListOfServiceModel();
        String descriptorId = "def1ce-4cf4-477c-aab3-2b04e6a382";
        ResponseEntity<List<ServiceModel>> allServiceModelResponse = new ResponseEntity<>(allServiceModel,
                HttpStatus.OK);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<ParameterizedTypeReference<List<ServiceModel>>>any()))
                .thenReturn(allServiceModelResponse);

        Optional<ServiceModel> returnedServiceModel = toscaoService.getServiceModelByDescriptorId(descriptorId);

        assertThat(returnedServiceModel).isPresent();
        Optional<NodeTemplate> nodeTemp = returnedServiceModel.get().getTopology().getNodeTemplates().stream()
                .filter(nodeTemplate -> nodeTemplate.getParentType().equals(Constants.TOSCA_NODES_NFV_VNF_TYPE)).findFirst();
        assertThat(nodeTemp).isPresent();
        Optional<NodeProperty> returnedDescriptorId = nodeTemp.get().getNodeProperties()
                .stream().filter(property -> property.getName().equals(DESCRIPTOR_ID)).findFirst();
        assertThat(returnedDescriptorId).isPresent();
        assertThat(returnedDescriptorId.get().getRawValue()).isEqualTo(descriptorId);
    }

    @Test
    public void testGetServiceModelByDescriptorIdNotPresent() throws Exception {
        List<ServiceModel> allServiceModel = getListOfServiceModel();
        String descriptorId = "xyz";
        ResponseEntity<List<ServiceModel>> allServiceModelResponse
                = new ResponseEntity<>(allServiceModel, HttpStatus.OK);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<ParameterizedTypeReference<List<ServiceModel>>>any()))
                .thenReturn(allServiceModelResponse);

        Optional<ServiceModel> returnedServiceModel = toscaoService.getServiceModelByDescriptorId(descriptorId);

        assertThat(returnedServiceModel).isNotPresent();
    }

    @Test
    public void testGetServiceModelByDescriptorIdReturnEmptyOptional() {
        String descriptorId = "testDescriptorId";
        ResponseEntity<List<ServiceModel>> emptyServiceModelsResponse =
                ResponseEntity.badRequest().build();

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(),
                ArgumentMatchers.<ParameterizedTypeReference<List<ServiceModel>>>any()))
                .thenReturn(emptyServiceModelsResponse);

        Optional<ServiceModel> actual = toscaoService.getServiceModelByDescriptorId(descriptorId);

        assertThat(actual).isEqualTo(Optional.empty());
    }

    @Test
    public void testGetServiceModelByDescriptorIdNull() {
        assertThatThrownBy(() -> toscaoService.getServiceModelByDescriptorId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ToscaoServiceImpl.DESCRIPTOR_ID_EMPTY_ERROR_MESSAGE);
    }

    @Test
    public void testGetServiceModelByServiceModelId() throws Exception {
        ServiceModel serviceModel = getServiceModel();
        String serviceModelId = "63811460-edca-4a70-be58-c8df34eba079";
        ResponseEntity<ServiceModel> serviceModelResponse = new ResponseEntity<>(serviceModel, HttpStatus.OK);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<Class<ServiceModel>>any())).thenReturn(serviceModelResponse);

        Optional<ServiceModel> returnedServiceModel = toscaoService.getServiceModelByServiceModelId(serviceModelId);

        assertThat(returnedServiceModel).isPresent();
        assertThat(returnedServiceModel.get().getId()).isEqualTo(serviceModelId);
    }

    @Test
    public void testGetServiceModelByServiceModelIdNull() {
        assertThatThrownBy(() -> toscaoService.getServiceModelByServiceModelId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ToscaoServiceImpl.SERVICE_MODEL_ID_EMPTY_ERROR_MESSAGE);
    }

    @Test
    public void testGetServiceModelByServiceModelIdWith5xxException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<Class<ServiceModel>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> toscaoService.getServiceModelByServiceModelId("xyz"))
                .isInstanceOf(ToscaoException.class)
                .hasMessage(String.format(ToscaoServiceImpl.ERROR_MESSAGE_QUERYING_SERVICE_MODEL,
                        HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void testGetServiceModelByServiceModelIdWith4xxException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<Class<ServiceModel>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> toscaoService.getServiceModelByServiceModelId("xyz"))
                .isInstanceOf(ToscaoException.class)
                .hasMessage(String.format(ToscaoServiceImpl.ERROR_MESSAGE_QUERYING_SERVICE_MODEL,
                        HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetServiceModelByServiceModelIdWithNotFoundException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<Class<ServiceModel>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.NOT_FOUND));

        Optional<ServiceModel> serviceModel = toscaoService.getServiceModelByServiceModelId("xyz");

        assertThat(serviceModel).isNotPresent();
    }

    @Test
    public void testGetServiceModelByServiceModelIdWithNoOk() throws Exception {
        ServiceModel serviceModel = getServiceModel();
        ResponseEntity<ServiceModel> serviceModelResponse = new ResponseEntity<>(serviceModel, HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<Class<ServiceModel>>any())).thenReturn(serviceModelResponse);

        Optional<ServiceModel> returnedServiceModel = toscaoService.getServiceModelByServiceModelId("serviceModelId");

        assertThat(returnedServiceModel).isNotPresent();
    }

    @Test
    public void testGetTypeDefinitions() throws Exception {
        TypeDefinitions typeDefinitions = getTpeDefinitions();
        String serviceModelId = "37866813-a0cb-4ad4-9716-a5b4fa19f940";
        ResponseEntity<TypeDefinitions> typeDefinitionsResponse = new ResponseEntity<>(typeDefinitions, HttpStatus.OK);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<Class<TypeDefinitions>>any())).thenReturn(typeDefinitionsResponse);

        Optional<TypeDefinitions> returnedTypeDefinitions = toscaoService.getTypeDefinitions(serviceModelId);

        assertThat(returnedTypeDefinitions).isPresent();
        assertThat(returnedTypeDefinitions.get().getServiceModelID()).isEqualTo(serviceModelId);
    }

    @Test
    public void testGetTypeDefinitionsWithServiceModelIdNull() {
        assertThatThrownBy(() -> toscaoService.getTypeDefinitions(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ToscaoServiceImpl.SERVICE_MODEL_ID_EMPTY_ERROR_MESSAGE);
    }

    @Test
    public void testGetTypeDefinitionsWith5xxException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<Class<TypeDefinitions>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> toscaoService.getTypeDefinitions("xyz"))
                .isInstanceOf(ToscaoException.class)
                .hasMessage(String.format(ToscaoServiceImpl.ERROR_MESSAGE_QUERYING_TYPE_DEFINITIONS,
                        HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void testGetTypeDefinitionsWith4xxException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<Class<TypeDefinitions>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> toscaoService.getTypeDefinitions("xyz"))
                .isInstanceOf(ToscaoException.class)
                .hasMessage(String.format(ToscaoServiceImpl.ERROR_MESSAGE_QUERYING_TYPE_DEFINITIONS,
                        HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetTypeDefinitionsWithNotFoundException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<Class<TypeDefinitions>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.NOT_FOUND));

        Optional<TypeDefinitions> typeDefinitions = toscaoService.getTypeDefinitions("xyz");

        assertThat(typeDefinitions).isNotPresent();
    }

    @Test
    public void testGetTypeDefinitionsWithNoOk() throws Exception {
        TypeDefinitions typeDefinitions = getTpeDefinitions();
        ResponseEntity<TypeDefinitions> typeDefinitionsResponse = new ResponseEntity<>(typeDefinitions,
                HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<Class<TypeDefinitions>>any())).thenReturn(typeDefinitionsResponse);

        Optional<TypeDefinitions> returnedTypeDefinitions = toscaoService.getTypeDefinitions("serviceModelId");

        assertThat(returnedTypeDefinitions).isNotPresent();
    }

    @Test
    public void testDeleteServiceModel() {
        ResponseEntity<String> deleteResponse = new ResponseEntity<>("typeDefinitions", HttpStatus.OK);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<Class<String>>any())).thenReturn(deleteResponse);
        assertThat(toscaoService.deleteServiceModel("service-model-id")).isTrue();
    }

    @Test
    public void testDeleteServiceModelNull() {
        assertThatThrownBy(() -> toscaoService.deleteServiceModel(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ToscaoServiceImpl.SERVICE_MODEL_ID_EMPTY_ERROR_MESSAGE);
    }

    @Test
    public void testDeleteServiceModelWith5xxException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<Class<String>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> toscaoService.deleteServiceModel("xyz"))
                .isInstanceOf(ToscaoException.class)
                .hasMessage(String.format(ToscaoServiceImpl.ERROR_MESSAGE_DELETING_SERVICE_MODEL, "xyz",
                        HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void testDeleteServiceModel4xxException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<Class<String>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> toscaoService.deleteServiceModel("xyz"))
                .isInstanceOf(ToscaoException.class)
                .hasMessage(String.format(ToscaoServiceImpl.ERROR_MESSAGE_DELETING_SERVICE_MODEL, "xyz",
                        HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testDeleteServiceModelWithNotFoundException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<Class<String>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.NOT_FOUND));

        assertThat(toscaoService.deleteServiceModel("xyz")).isTrue();
    }

    @Test
    public void testDeleteServiceModelWithNotFoundReturned() {
        ResponseEntity<String> notFoundResponse = new ResponseEntity<>("typeDefinitions", HttpStatus.NOT_FOUND);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<Class<String>>any()))
                .thenReturn(notFoundResponse);

        assertThat(toscaoService.deleteServiceModel("service-model-id")).isTrue();
    }

    @Test
    public void testDeleteServiceModelWithRetry() {
        ResponseEntity<String> deleteResponse = new ResponseEntity<>("typeDefinitions", HttpStatus.OK);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<Class<String>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                .thenReturn(deleteResponse);

        assertThat(toscaoService.deleteServiceModel("service-model-id")).isTrue();
    }

    @Test
    public void testDeleteServiceModelWithNoOk() throws Exception {
        ResponseEntity<String> deleteResponse = new ResponseEntity<>("String", HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<Class<String>>any())).thenReturn(deleteResponse);

        assertThat(toscaoService.deleteServiceModel("serviceModelId")).isFalse();
    }

    @Test
    public void testUploadPackageToToscao() throws Exception {
        ServiceModel serviceModel = getServiceModel();
        ResponseEntity<ServiceModel> serviceModelResponse = new ResponseEntity<>(serviceModel, HttpStatus.OK);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<Class<ServiceModel>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST))
                .thenReturn(serviceModelResponse);

        Optional<ServiceModel> returnedServiceModel = toscaoService.uploadPackageToToscao(
                TestUtils.getResource(SERVICE_MODEL_SAMPLE_RESPONSE).toAbsolutePath());

        assertThat(returnedServiceModel).isPresent();
    }

    @Test
    public void testUploadPackageToToscaoWithPathIsNull() {
        assertThatThrownBy(() -> toscaoService.uploadPackageToToscao(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ToscaoServiceImpl.ERROR_MESSAGE_CSAR_PATH_IS_NULL);
    }

    @Test
    public void testUploadPackageToToscaoWith5xxException() {
        final Path sampleResponse = TestUtils.getResource(SERVICE_MODEL_SAMPLE_RESPONSE).toAbsolutePath();
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<Class<ServiceModel>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> toscaoService.uploadPackageToToscao(
                sampleResponse))
                .isInstanceOf(ToscaoException.class)
                .hasMessage(String.format(ToscaoServiceImpl.ERROR_MESSAGE_UPLOADING_CSAR,
                        HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void testUploadPackageToToscaoWith4xxException() {
        final Path sampleResponse = TestUtils.getResource(SERVICE_MODEL_SAMPLE_RESPONSE).toAbsolutePath();

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<Class<ServiceModel>>any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> toscaoService.uploadPackageToToscao(
                sampleResponse))
                .isInstanceOf(ToscaoException.class)
                .hasMessage(String.format(ToscaoServiceImpl.ERROR_MESSAGE_UPLOADING_CSAR,
                        HttpStatus.BAD_REQUEST));

        verify(restTemplate, times(3)).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<Class<ServiceModel>>any());
    }

    @Test
    public void testUploadPackageToToscaoWithNoOk() throws Exception {
        ServiceModel serviceModel = getServiceModel();
        ResponseEntity<ServiceModel> serviceModelResponse = new ResponseEntity<>(serviceModel, HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
                ArgumentMatchers.<Class<ServiceModel>>any())).thenReturn(serviceModelResponse);

        Optional<ServiceModel> returnedServiceModel = toscaoService.uploadPackageToToscao(
                TestUtils.getResource(SERVICE_MODEL_SAMPLE_RESPONSE).toAbsolutePath());

        assertThat(returnedServiceModel).isNotPresent();
    }

    @Test
    public void testHealthStatusWithToscaoServiceUp() {
        String expected = "{health : true}";
        ResponseEntity<String> healthResponse = ResponseEntity.ok()
                .build();

        when(restTemplate.getForEntity(HEALTH_CHECK_URL, String.class))
                .thenReturn(healthResponse);

        String actual = toscaoService.healthStatus();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testHealthStatusWithToscaoServiceDown() {
        String expected = "{health : false}";
        ResponseEntity<String> healthResponse = ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        when(restTemplate.getForEntity(HEALTH_CHECK_URL, String.class))
                .thenReturn(healthResponse);

        String actual = toscaoService.healthStatus();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testHealthStatusWithToscaoServiceUnavailable() {
        String expected = "{health : false}";

        when(restTemplate.getForEntity(HEALTH_CHECK_URL, String.class))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        String actual = toscaoService.healthStatus();

        assertThat(actual).isEqualTo(expected);
    }

    private List<ServiceModel> getListOfServiceModel() throws Exception {
        return TestUtils.readListFromFile(ALL_SERVICE_MODEL_SAMPLE_RESPONSE, ServiceModel.class);
    }

    private ServiceModel getServiceModel() throws Exception {
        return TestUtils.readObjectFromFile(SERVICE_MODEL_SAMPLE_RESPONSE, ServiceModel.class);
    }

    private TypeDefinitions getTpeDefinitions() throws Exception {
        return TestUtils.readObjectFromFile(TYPE_DEFINITIONS_SAMPLE_RESPONSE, TypeDefinitions.class);
    }

    private void mockRetryTemplateResult() {
        RetryTemplate retryTemplate = new RetryTemplate();

        TimeoutRetryPolicy timeoutRetryPolicy = new TimeoutRetryPolicy();
        timeoutRetryPolicy.setTimeout(1000L);

        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(3);

        CompositeRetryPolicy compositeRetryPolicy = new CompositeRetryPolicy();
        compositeRetryPolicy.setPolicies(new RetryPolicy[]{timeoutRetryPolicy, simpleRetryPolicy});
        retryTemplate.setRetryPolicy(compositeRetryPolicy);

        ReflectionTestUtils.setField(toscaoService, "retryToscaTemplate", retryTemplate);
    }
}
