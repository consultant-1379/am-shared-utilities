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

import com.ericsson.am.shared.vnfd.model.servicemodel.ServiceModel;
import com.ericsson.am.shared.vnfd.model.typedefinition.TypeDefinitions;
import com.ericsson.am.shared.vnfd.service.exception.ToscaoException;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface ToscaoService {

    /**
     * List all the service models.
     */
    Optional<List<ServiceModel>> getServiceModels() throws ToscaoException;

    /**
     * Return service model by descriptor id
     *
     * @param descriptorId
     */
    Optional<ServiceModel> getServiceModelByDescriptorId(String descriptorId) throws ToscaoException;


    /**
     * Returns a type definition for the provided input
     *
     * @param serviceModelID
     * @return TypeDefinitions
     */
    Optional<TypeDefinitions> getTypeDefinitions(String serviceModelID) throws ToscaoException;

    /**
     * Upload CSARs to a TOSCA-O
     *
     * @param file
     * @return Optional<ServiceModel>, Returns the service model
     */
    Optional<ServiceModel> uploadPackageToToscao(Path file) throws ToscaoException;

    /**
     * Deletes the service model provided by service model id
     *
     * @param serviceModelId
     * @return boolean, true if the delete is successful
     */
    boolean deleteServiceModel(String serviceModelId) throws ToscaoException;

    /**
     * Return the Service model if the service model id is present
     *
     * @param serviceModelId
     * @return Optional<ServiceModel>
     */
    Optional<ServiceModel> getServiceModelByServiceModelId(String serviceModelId) throws ToscaoException;

    /**
     * Check the health of TOSCA-O
     *
     * @return health check response
     */
    String healthStatus();
}
