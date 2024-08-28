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
package com.ericsson.am.shared.vnfd;

import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Flavour;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Node;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.ParentVnfd;
import com.ericsson.am.shared.vnfd.utils.Constants;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONObject;

import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeJsonObject;
import static com.ericsson.am.shared.vnfd.FlavourUtility.validateFlavours;
import static com.ericsson.am.shared.vnfd.utils.Constants.DATA_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.SUBSTITUTION_MAPPINGS;

public final class NestedVnfdUtility {

    private NestedVnfdUtility() {
    }

    public static ParentVnfd createNestedVnfdObjects(Path parentVnfdPath, JSONObject parentVnfdJson, VnfDescriptorDetails vnfDescriptorDetails) {
        ParentVnfd parentVnfd = createParentVnfd(parentVnfdJson);
        List<Path> descriptorFiles;
        try (Stream<Path> walk = Files.walk(Paths.get(parentVnfdPath.getParent().toAbsolutePath().toString()))) {
            descriptorFiles = walk.map(Path::toString)
                    .filter(f -> f.endsWith(".yaml"))
                    .filter(f -> !f.contains("etsi_nfv_sol001_vnfd_2_5_1_types.yaml"))
                    .filter(f -> !f.contains(parentVnfdPath.toFile().getName()))
                    .map(f -> parentVnfdPath.toAbsolutePath().getParent().resolve(f))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to parse descriptor files", e);
        }
        for (Path path : descriptorFiles) {
            JSONObject file = VnfdUtility.validateYamlCanBeParsed(path);
            if (isFlavourFile(file)) {
                Flavour flavourToAdd = FlavourUtility.getFlavour(file);
                parentVnfd.getFlavours().put(flavourToAdd.getId(), flavourToAdd);
            } else if (isNodeFile(file)) {
                Node node = NodeUtility.getNode(file, vnfDescriptorDetails);
                parentVnfd.setNode(node);
            }
        }
        if (parentVnfd.getNode() == null || parentVnfd.getFlavours().isEmpty()) {
            throw new IllegalArgumentException("Vnfd does not reference both node and flavour files.");
        }
        checkFlavoursPresentInParentVnfd(parentVnfd.getNode().getNodeType().getNodeProperties().getValidFlavourIds(), parentVnfd.getFlavours());
        validateFlavours(parentVnfd);
        setFlavoursInDescriptorDetails(parentVnfd, vnfDescriptorDetails);
        return parentVnfd;
    }

    private static void checkFlavoursPresentInParentVnfd(Map<String, Boolean> nodeFlavours, Map<String, Flavour> parentVnfdflavours) {
        for (String nodeFlavour : nodeFlavours.keySet()) {
            if (!parentVnfdflavours.keySet().contains(nodeFlavour)) {
                throw new IllegalArgumentException(String.format("flavour %s not defined in Parent Vnfd", nodeFlavour));
            }
        }
    }

    private static void setFlavoursInDescriptorDetails(ParentVnfd parentVnfd, VnfDescriptorDetails descriptorDetails) {
        Map<String, Flavour> flavourMap = parentVnfd.getFlavours();
        descriptorDetails.setFlavours(flavourMap);
        Optional<Map.Entry<String, Boolean>> defaultFlavour =
                parentVnfd.getNode().getNodeType().getNodeProperties().getValidFlavourIds()
                        .entrySet()
                        .stream().filter(e -> Boolean.TRUE.equals(e.getValue())).findFirst();
        defaultFlavour.ifPresent(stringBooleanEntry -> descriptorDetails.setDefaultFlavour(parentVnfd.getFlavours()
                .get(stringBooleanEntry.getKey())));
    }

    public static boolean isFlavourFile(JSONObject jsonObject) {
        Optional<JSONObject> topologyTemplateOpt = VnfdUtility.getTopologyTemplate(jsonObject);
        if (!topologyTemplateOpt.isPresent()) {
            return false;
        }
        JSONObject topologyTemplateJson = topologyTemplateOpt.get();

        if (hasPropertyOfTypeJsonObject(topologyTemplateJson, SUBSTITUTION_MAPPINGS)) {
            JSONObject substitutionMappings = topologyTemplateJson.getJSONObject(SUBSTITUTION_MAPPINGS);
            return hasPropertyOfTypeJsonObject(substitutionMappings, PROPERTIES_KEY)
                    && substitutionMappings.getJSONObject(PROPERTIES_KEY).keySet().contains(Constants.FLAVOUR_ID_KEY)
                    && VnfdUtility.checkKeyExists(substitutionMappings, NODE_TYPE_KEY);
        }
        return false;
    }

    public static boolean isNodeFile(JSONObject jsonObject) {
        return hasPropertyOfTypeJsonObject(jsonObject, DATA_TYPES_KEY);
    }

    public static ParentVnfd createParentVnfd(JSONObject vnfd) {
        return CommonUtility.getPojo(vnfd, ParentVnfd.class);
    }
}
