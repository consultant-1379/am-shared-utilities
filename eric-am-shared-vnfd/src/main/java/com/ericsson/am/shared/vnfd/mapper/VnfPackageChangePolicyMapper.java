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
package com.ericsson.am.shared.vnfd.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.am.shared.vnfd.model.policies.CallOperationTrigger;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyCommon;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyTosca1dot2;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyTosca1dot3;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangeProperty;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangeSelector;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangeTriggerTosca1dot2;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangeTriggerTosca1dot3;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class VnfPackageChangePolicyMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(VnfPackageChangePolicyMapper.class);

    private VnfPackageChangePolicyMapper() {
    }

    public static VnfPackageChangePolicyTosca1dot2 convert(VnfPackageChangePolicyTosca1dot3 vnfPackageChangePolicyTosca1dot3) {
        VnfPackageChangePolicyTosca1dot2 vnfPackageChangePolicy = new VnfPackageChangePolicyTosca1dot2();
        vnfPackageChangePolicy.setProperties(vnfPackageChangePolicyTosca1dot3.getProperties());
        vnfPackageChangePolicy.setType(vnfPackageChangePolicyTosca1dot3.getType());
        List<Map<String, VnfPackageChangeTriggerTosca1dot2>> vnfPkgTriggerMap = buildVnfPkgTriggerMap(vnfPackageChangePolicyTosca1dot3);
        vnfPackageChangePolicy.setTriggers(vnfPkgTriggerMap);
        return vnfPackageChangePolicy;
    }

    public static void convert(JSONObject tempPolicy, VnfPackageChangePolicyCommon vnfPackageChangePolicy) throws JsonProcessingException {
        final JSONObject properties = tempPolicy.getJSONObject("properties");
        final Object selector = properties.get("selector");

        List<VnfPackageChangeSelector> result = new ArrayList<>();

        try {
            VnfPackageChangeSelector temp = new ObjectMapper().readValue(selector.toString(), VnfPackageChangeSelector.class);
            result.add(temp);
        } catch (JsonMappingException e) {
            LOGGER.warn("Failed to convert selector {} as object. Tried to convert as List. Error message: {}", selector, e.getMessage());
            result = new ObjectMapper().readValue(selector.toString(), new TypeReference<>() {
            });
        }

        final VnfPackageChangeProperty vnfPackageChangePolicyProperties = vnfPackageChangePolicy.getProperties();
        vnfPackageChangePolicyProperties.setVnfPackageChangeSelectors(result);
    }

    private static List<Map<String, VnfPackageChangeTriggerTosca1dot2>> buildVnfPkgTriggerMap(VnfPackageChangePolicyTosca1dot3
                                                                                                      vnfPackageChangePolicyTosca1dot3) {
        List<Map<String, VnfPackageChangeTriggerTosca1dot2>> listOfVnfPkgTriggerMaps = new ArrayList<>();

        vnfPackageChangePolicyTosca1dot3.getTriggers().forEach((key, value) -> {
            for (CallOperationTrigger callOperationTrigger : value.getAction()) {
                Map<String, VnfPackageChangeTriggerTosca1dot2> vnfPackageChangeTriggerMap = new HashMap<>();

                vnfPackageChangeTriggerMap.put(key, buildVnfPackageChangeTrigger(value, callOperationTrigger));

                listOfVnfPkgTriggerMaps.add(vnfPackageChangeTriggerMap);
            }
        });
        return listOfVnfPkgTriggerMaps;
    }

    private static VnfPackageChangeTriggerTosca1dot2 buildVnfPackageChangeTrigger(VnfPackageChangeTriggerTosca1dot3 value,
                                                                                  CallOperationTrigger callOperationTrigger) {
        VnfPackageChangeTriggerTosca1dot2 vnfPackageChangeTrigger = new VnfPackageChangeTriggerTosca1dot2();
        vnfPackageChangeTrigger.setAction(callOperationTrigger.getCallOperation());
        vnfPackageChangeTrigger.setEvent(value.getEvent());
        return vnfPackageChangeTrigger;
    }
}
