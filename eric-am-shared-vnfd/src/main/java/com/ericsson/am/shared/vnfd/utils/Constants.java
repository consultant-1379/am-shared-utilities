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
package com.ericsson.am.shared.vnfd.utils;

import java.util.Arrays;
import java.util.List;

import com.ericsson.am.shared.vnfd.VnfPackageChangePatternCommand;

public final class Constants {

    public static final String ARTIFACT_KEYS = "artifactKeys";
    public static final String VNFD = "vnfd";
    public static final String VNFD_TOSCA_1_3_VERSION = "tosca_simple_yaml_1_3";

    public static final String TOSCA_NODES_NFV_VNF_TYPE = "tosca.nodes.nfv.VNF";
    public static final String TOSCA_INTERFACES_NFV_VNFLCM_TYPE = "tosca.interfaces.nfv.Vnflcm";
    public static final String TOSCSA_ARTIFACTS_NFV_SW_IMAGE_TYPE = "tosca.artifacts.nfv.SwImage";
    public static final String VNF_INFO_MODIFIABLE_ATTRIBUTES_TYPE
            = "ericsson.datatypes.nfv.VnfInfoModifiableAttributes";
    public static final String TOSCA_INTERFACES_NFV_CHANGE_CURRENT_VNF_PACKAGE_TYPE
            = "tosca.interfaces.nfv.ChangeCurrentVnfPackage";
    public static final String TOSCA_DATATYPE_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_TYPE
            = "tosca.datatypes.nfv.VnfInfoModifiableAttributes";
    public static final String TOSCA_DATATYPES_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_EXTENSIONS_TYPE =
            "tosca.datatypes.nfv.VnfInfoModifiableAttributesExtensions";
    public static final String ERIC_DATATYPES_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_EXTENSIONS_TYPE =
            "ericsson.datatypes.nfv.VnfInfoModifiableAttributesExtensions";

    public static final String HELM_PACKAGES_PRIORITY = "helm_packages_priority";
    public static final String HELM_VALUES = "helm_values";
    public static final String HELM_PACKAGE_PREFIX = "helm";
    public static final String CRD_PACKAGE_PREFIX = "crd";
    public static final String SUBSTITUTION_MAPPINGS = "substitution_mappings";
    public static final String TOSCA_FILE_TYPE = "tosca.artifacts.File";
    public static final String CHART_PARAM = "chart_param";
    public static final String FULL_RESTORE = "Full Restore";
    public static final String LCM_OPERATIONS_CONFIGURATION = "lcm_operations_configuration";
    public static final String MANUAL_CONTROLLED = "ManualControlled";
    public static final String CISM_CONTROLLED = "CISMControlled";
    public static final String MAP = "map";

    public static final String VNFD_TOSCA_DEFINITION_VERSION_KEY = "tosca_definitions_version";
    public static final String ADDITIONAL_PARAMETERS_KEY = "additional_parameters";
    public static final String ARTIFACTS_KEY = "artifacts";
    public static final String ASSOCIATED_ARTIFACTS_KEY = "associatedArtifacts";
    public static final String SCALING_MAPPING_KEY = "scaling_mapping";
    public static final String DATA_TYPES_KEY = "data_types";
    public static final String DERIVED_FROM_KEY = "derived_from";
    public static final String VERSION_KEY = "version";
    public static final String OPERATIONS_KEY = "operations";
    public static final String FLAVOUR_ID_KEY = "flavour_id";
    public static final String HELM_PACKAGE_KEY = "helm_package";
    public static final String HELM_PACKAGES_KEY = "helm_packages";
    public static final String INSTANTIATE_KEY = "instantiate";
    public static final String INPUTS_KEY = "inputs";
    public static final String UNSUPPORTED_OPERATION_TYPE = "unsupportedOperationType";
    public static final String ROLLBACK = "rollback";
    public static final String INTERFACES_KEY = "interfaces";
    public static final String NODE_TEMPLATES_KEY = "node_templates";
    public static final String NODE_TYPE_KEY = "node_type";
    public static final String NODE_TYPES_KEY = "node_types";
    public static final String INTERFACE_TYPES_KEY = "interface_types";
    public static final String POLICIES_KEY = "policies";
    public static final String MODIFIABLE_ATTRIBUTES = "modifiable_attributes";
    public static final String POLICY_TYPE_KEY = "type";
    public static final String PROPERTIES_KEY = "properties";
    public static final String TOPOLOGY_TEMPLATE_KEY = "topology_template";
    public static final String TYPE_KEY = "type";
    public static final String NAME_KEY = "name";
    public static final String DEPLOYABLE_MODULES_PROPERTY = "deployableModules";
    public static final String DEPLOYABLE_MODULES_ENABLED = "enabled";
    public static final String DEPLOYABLE_MODULES_DISABLED = "disabled";
    public static final String TRIGGERS_KEY = "triggers";
    public static final String DEFAULT_KEY = "default";
    public static final String DESCRIPTION_KEY = "description";
    public static final String REQUIRED_KEY = "required";
    public static final String CONSTRAINTS_KEY = "constraints";
    public static final String METADATA_KEY = "metadata";
    public static final String VALID_VALUES_KEY = "valid_values";
    public static final String EXTENSIONS_KEY = "extensions";
    public static final String VNF_CONTROLLED_SCALING_PROPERTY = "vnfControlledScaling";
    public static final String ENTRY_SCHEMA_KEY = "entry_schema";
    public static final String REQUIREMENTS_KEY = "requirements";
    public static final String DEPLOYABLE_MODULE_ASSOCIATED_ARTIFACTS = "associatedArtifacts";
    public static final String FILE_KEY = "file";
    public static final String VIRTUAL_STORAGE = "virtual_storage";
    public static final String CONTAINER = "container";
    public static final String VIRTUAL_LINK = "virtual_link";
    public static final String VIRTUAL_BINDING = "virtual_binding";
    public static final String TARGET = "target";
    public static final String ADDITIONAL_PARAMETERS_NOT_PROVIDED_FOR_INPUTS_ERROR_MESSAGE =
            "additional_parameters not provided for input in interface %s";
    public static final String ASPECT_DELTA_NOT_PRESENT_IN_VNFD = "VduScalingAspect not defined in the descriptor";
    public static final String SCALING_ASPECTS_NOT_PRESENT_IN_INSTANTIATION_LEVELS =
            "Not all instantiation level aspects defined in scaling aspects";
    public static final String ARTIFACTS_DETAILS_NOT_PRESENT_IN_THE_NODE_TYPE =
            "Artifacts details not present in the node type";
    public static final String DATA_TYPE_NOT_DEFINED_ERROR_MESSAGE = "Data type not defined for %s";
    public static final String DERIVED_FROM_NOT_DEFINED_ERROR_MESSAGE = "derived_from not defined for node %s";
    public static final String DERIVED_FROM_VALUE_NOT_SUPPORTED =
            DERIVED_FROM_KEY + " only supports " + TOSCA_NODES_NFV_VNF_TYPE;
    public static final String DOCKER_IMAGES_NOT_PRESENT = "Docker images are not defined in the artifacts section";
    public static final String DUPLICATE_VDU_IN_INITIAL_DELTA =
            "There are duplicate vdu members in the initial delta, this is not supported";
    public static final String EMPTY_JSON_OBJECT_PROVIDED = "Empty json object provided";
    public static final String FILE_NOT_PROVIDED_ERROR_MESSAGE = "file location not provided for artifact ";
    public static final String INTERFACE_TYPE_NOT_DEFINED = "%s interface type not defined";

    public static final String HELM_CHARTS_NOT_PRESENT = "helm chart details not provided in the artifacts section of the vnfd";
    public static final String CNF_CHARTS_NOT_PRESENT = "at least one CNF chart should be provided in the artifacts section of the vnfd";
    public static final String HELM_PACKAGE_MISSING_IN_ARTIFACTS = "helm package id %s is missing from artifacts";
    public static final String HELM_PACKAGES_MISSING_IN_FLAVOUR_FILE = "helm_packages missing in flavour file";
    public static final String INITIAL_DELTA_NOT_PRESENT_IN_VNFD = "Initial delta not defined in the descriptor";
    public static final String EMPTY_POLICY_KEY_ERROR_MESSAGE = "Empty policy key defined without policy";
    public static final String NOT_PROVIDED_IN_VNFD_ERROR_MESSAGE = "%s not provided in the %s";
    public static final String INSTANTIATE_USECASE_NOT_PRESENT_ERROR_MESSAGE = "Instantiate use-case not defined in the interfaces section";
    public static final String INTERFACE_DETAILS_NOT_PRESENT_IN_THE_NODE_TYPE = "Interface details not present in the node type";
    public static final String INVALID_INPUT_PROVIDED_FOR_INPUTS = "Invalid inputs provided to interface %s";
    public static final String INVALID_NODE_DETAILS_PROVIDED = "Invalid node details provided";
    public static final String MISSING_VDU = "Missing vdu in targets section either in initial delta or step delta";
    public static final String MORE_THAN_ONE_NODE_TYPE_ERROR_MESSAGE = "Node type can be only one";
    public static final String VNFLCM_INTERFACE_NOT_PRESENT_ERROR_MESSAGE = "tosca.interfaces.nfv.Vnflcm interface not defined";
    public static final String NON_SCALABLE_VDU_MISSING_IN_VDU_INSTANTIATION_LEVELS = "NonScalable VDU-s are missing in VduInstantiationLevels: %s";
    public static final String OPERATION_MISSING_IN_VNF_LCM_INTERFACES = "%s operation is missing in VNF LCM interfaces";
    public static final String ROLLBACK_OPERATION_REQUIRES_INTERFACE_DERIVED_FROM_TOSCA_CCVP =
            "For Rollback LCM operation it's required that VNFD to contain Interface derived from "
                    + TOSCA_INTERFACES_NFV_CHANGE_CURRENT_VNF_PACKAGE_TYPE;
    public static final String EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED = "Exception happened during check if operation is supported: ";
    public static final String VNFD_CONTAINS_ONLY_NON_SCALABLE_VDU = "VNFD contains only non-scalable VDUs";
    public static final String NODE_PROPERTY_MISSING_ERROR_MESSAGE = "Property missing for the node ";
    public static final String NODE_TEMPLATES_MISSING = "node_templates missing from %s";
    public static final String NODE_TYPE_DETAILS_NOT_PRESENT_ERROR_MESSAGE = "None of the node details are present";
    public static final String NODE_TYPE_NOT_PRESENT_ERROR_MESSAGE = "node_types attribute not present in vnfd";
    public static final String NOT_A_VALID_INTERFACE = "Interface %s is not a valid interface";
    public static final String ARTIFACTS_FOR_THE_FOLLOWING_FLAVOUR_IDS_S_ARE_INVALID = "Artifacts for the following flavour ids %s are invalid";
    public static final String INTERFACE_DEFINED_IGNORING_INPUTS = "Flavour id {} has interface {} defined, ignoring inputs";
    public static final String PROPERTIES_NOT_NULL_ERROR_MESSAGE = "Properties for type %s cannot be null or empty";
    public static final String SCALING_ASPECT_NOT_PRESENT_IN_VNFD = "Scaling aspect not defined in the descriptor";
    public static final String SCALING_DELTA_NOT_DEFINED = "Scaling delta %s is not defined for scaling aspect %s in VduScalingDelta %s";
    public static final String TOPOLOGY_TEMPLATE_MISSING = "topology_template missing from flavour file";
    public static final String TYPE_NOT_DEFINED_FOR_ADDITIONAL_PARAMETERS = "Type not defined for additional parameter in interface %s";
    public static final String TYPES_MISSING_FROM_FLAVOUR = "types missing from flavour: %s";
    public static final String UNABLE_TO_PARSE_JSON = "Unable to parse VNFD, error message is: %s";
    public static final String VNFD_NOT_PRESENT_IN_THE_PATH_SPECIFIED = "vnfd not present in the path specified";
    public static final String UNABLE_TO_PARSE_YAML_FILE_ERROR_MESSAGE = "Unable to parse yaml file, please provide a valid yaml";
    public static final String VDU_SCALING_DELTA_NOT_DEFINED = "VduScalingDelta not defined for aspect %s";
    public static final String VNFD_NOT_MAP_FORMAT = "VNFD file is either invalid or is not provided.";
    public static final String INVALID_HEAL_CAUSES = "Causes for Heal must not be empty and must contain %s";
    public static final String INVALID_LCM_OPERATIONS_HEAL = "Incorrect lcm_operations_configuration for Heal Interface";
    public static final String LCM_OPERATIONS_MANDATORY = "lcm_operations_configuration is mandatory for a Heal Interface";
    public static final String INVALID_ENTRY_SCHEMA_ERROR_MESSAGE = "invalid entry_schema definition provided for property %s";
    public static final String INVALID_CONSTRAINT_ERROR_MESSAGE = "invalid constraint definition provided for property %s";
    public static final String PROPERTIES_NULL_FOR_DATATYPE_ERROR_MESSAGE = "Properties can't be null or empty for data type %s";
    public static final String EXTENSION_CANT_BE_NULL = "extensions can't be null for data type %s";
    public static final String INVALID_EXTENSION_DATA_TYPE_ERROR_MESSAGE = "Extension of data type %s should be of type "
            + TOSCA_DATATYPES_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_EXTENSIONS_TYPE;
    public static final String PROPERTIES_CANT_BE_NULL_ERROR_MESSAGE = "Properties can't be null or empty for data type %s";
    public static final String VNF_CONTROLLED_SCALING_PROPERTY_MISSING = "vnfControlledScaling missing in property for data type %s";
    public static final String EXTENSION_PROPERTIES_INVALID_TYPE_ERROR_MESSAGE = "vnfControlledScaling and deployableModules properties from %s "
            + "should have their \"type\" property declared as map";
    public static final String VNF_CONTROLLED_SCALING_DEFAULT_VALUE_MISSING = "vnfControlledScaling should have a " +
            "default value of data type %s";
    public static final String EXTENSION_PROPERTY_DEFAULT_VALUE_NOT_MAP_FORMAT = "Default value for %1$s " +
            "should be a map of key and value as %2$s or %3$s";
    public static final String VNF_CONTROLLED_SCALING_DEFAULT_VALUE_NOT_MAP =
            String.format(EXTENSION_PROPERTY_DEFAULT_VALUE_NOT_MAP_FORMAT, VNF_CONTROLLED_SCALING_PROPERTY, CISM_CONTROLLED, MANUAL_CONTROLLED);
    public static final String DEPLOYABLE_MODULES_DEFAULT_VALUE_NOT_MAP =
            String.format(EXTENSION_PROPERTY_DEFAULT_VALUE_NOT_MAP_FORMAT,
                          DEPLOYABLE_MODULES_PROPERTY,
                          DEPLOYABLE_MODULES_ENABLED,
                          DEPLOYABLE_MODULES_DISABLED);
    public static final String EXTENSIONS_DEFAULT_VALUE_BLANK_FOR_KEY_FORMAT = "%1$s's value is blank for " +
            "%2$s, it should have a value of either %3$s or %4$s";
    public static final String VNF_CONTROLLED_SCALING_DEFAULT_VALUE_BLANK_FOR_ASPECT_FORMAT =
            String.format(EXTENSIONS_DEFAULT_VALUE_BLANK_FOR_KEY_FORMAT, "%s", VNF_CONTROLLED_SCALING_PROPERTY, CISM_CONTROLLED, MANUAL_CONTROLLED);
    public static final String DEPLOYABLE_MODULES_DEFAULT_VALUE_BLANK_FOR_KEY_FORMAT =
            String.format(EXTENSIONS_DEFAULT_VALUE_BLANK_FOR_KEY_FORMAT,
                          "%s",
                          DEPLOYABLE_MODULES_PROPERTY, DEPLOYABLE_MODULES_ENABLED, DEPLOYABLE_MODULES_DISABLED);
    public static final String INVALID_VALUE_FOR_KEY_EXTENSION_PROPERTY_FORMAT = "%1$s's value is %2$s for " +
            "%3$s, it should have a value either %4$s or %5$s";
    public static final String INVALID_VALUE_FOR_ASPECT_IN_VNF_CONTROLLED_SCALING_FORMAT =
            String.format(INVALID_VALUE_FOR_KEY_EXTENSION_PROPERTY_FORMAT,
                          "%s",
                          "%s",
                          VNF_CONTROLLED_SCALING_PROPERTY,
                          CISM_CONTROLLED,
                          MANUAL_CONTROLLED);
    public static final String INVALID_VALUE_FOR_KEY_IN_DEPLOYABLE_MODULES_DEFAULTS_FORMAT =
            String.format(INVALID_VALUE_FOR_KEY_EXTENSION_PROPERTY_FORMAT,
                          "%s",
                          "%s",
                          DEPLOYABLE_MODULES_PROPERTY,
                          DEPLOYABLE_MODULES_ENABLED,
                          DEPLOYABLE_MODULES_DISABLED);
    public static final String ENTRY_SCHEMA_NULL_FOR_EXTENSIONS_PROPERTIES = "Entry schema can't be null " +
            "for properties for data type %s";
    public static final String ENTRY_SCHEMA_CONSTRAINT_CANT_BE_BLANK_FOR_EXTENSIONS_PROPERTIES = "Entry schema " +
            "constraints can't be blank for properties for data type %s";
    public static final String INVALID_ENTRY_SCHEMA_CONSTRAINTS_VALUE_FORMAT = "Constraints for %1$s should" +
            " only be - valid_values: [ \"%2$s\", \"%3$s\" ]";
    public static final String INVALID_VNF_CONTROLLED_SCALING_ENTRY_SCHEMA_CONSTRAINTS_VALUE = String.format(
            INVALID_ENTRY_SCHEMA_CONSTRAINTS_VALUE_FORMAT, VNF_CONTROLLED_SCALING_PROPERTY, CISM_CONTROLLED, MANUAL_CONTROLLED);
    public static final String INVALID_DEPLOYABLE_MODULES_ENTRY_SCHEMA_CONSTRAINTS_VALUE = String.format(
            INVALID_ENTRY_SCHEMA_CONSTRAINTS_VALUE_FORMAT, DEPLOYABLE_MODULES_PROPERTY, DEPLOYABLE_MODULES_ENABLED, DEPLOYABLE_MODULES_DISABLED);
    public static final String INVALID_PROPERTY_KEY_ERROR_MESSAGE = "%s is not valid property attribute for the " +
            "property key %s";

    public static final String VDU_COMPUTE_NODE = "tosca.nodes.nfv.Vdu.Compute";
    public static final String DEPLOYABLE_MODULES_NODE = "tosca.nodes.nfv.DeployableModule";
    public static final String MCIOP_NODE = "tosca.nodes.nfv.Mciop";
    public static final String OC_CONTAINER_NODE = "tosca.nodes.nfv.Vdu.OsContainer";
    public static final String VDU_OS_CONTAINER_DEPLOYABLE_UNIT_NODE = "tosca.nodes.nfv.Vdu.OsContainerDeployableUnit";
    public static final String VDU_VIRTUAL_BLOCK_STORAGE = "tosca.nodes.nfv.Vdu.VirtualBlockStorage";
    public static final String VDU_CP_NODE = "tosca.nodes.nfv.VduCp";
    public static final String VIRTUAL_CP_NODE = "tosca.nodes.nfv.VirtualCp";
    public static final String DEPLOYABLE_MODULES = "tosca.nodes.nfv.DeployableModule";
    public static final String VDU_INITIALDELTA_TARGETS_NOT_IN_COMPUTE_NAMES = "tosca.policies.nfv.VduInitialDelta has target name/s "
            + "%s which is not defined as a %s node.";
    public static final String IGNORING_POLICY_OF_TYPE = "Ignoring policy of type {}";
    public static final String VIOLATIONS_FOR_POLICY = "Checking violation for policy {}";
    public static final String VALIDATING_POLICIES = "Validating policies for {}";
    public static final String UNABLE_TO_PARSE_POLICIES = "Unable to parse scaling policies {}";
    public static final String CHECKING_VIOLATIONS = "Checking violation for policy key {} with body {}";
    public static final String VDU_INSTANTIATION_LEVELS_TARGETS_NOT_IN_COMPUTE_NAMES = "tosca.policies.nfv.VduInstantiationLevels has target name/s "
            + "%s which is not defined as a %s node.";
    public static final String HELMCHART_IN_PATTERN_NOT_FOUND_IN_ARTIFACTS = "Helmchart %s with command %s defined in"
            + " vnf package change pattern not found in artifacts section of vnfd";
    public static final String PARSING_ISSUE_WITH_ROLLBACK_FAILURE_PATTERN = "Rollback/failure pattern is incorrectly defined, unable to parse";
    public static final String MISSING_FAILURE_PATTERN = "Failure pattern for chart %s is missing";
    public static final String UNSUPPORTED_COMMAND_IN_PATTERN = "Command %s for helm chart %s defined in"
            + " vnf package change pattern not supported. The supported commands are: " + Arrays.asList(VnfPackageChangePatternCommand.values());
    public static final String VNF_PACKAGE_CHANGE_POLICY_NOT_DEFINED_FOR_VNF_PACKAGE_CHANGE_INTERFACE
            = "VNF package change policy not defined for VNF package change interface";
    public static final String VNF_PACKAGE_CHANGE_OPERATION_DEFINITION_MISSING_IN_POLICY =
            "Vnf Package Change operation definition missing in policy";
    public static final String WRONG_PATTERN_FOR_POLICY_OPERATION = "VnfPackageChange policy operation {} contains " +
            "wrong pattern {} for policy modification qualifier {}";
    public static final String EXTRA_VNF_PACKAGE_CHANGE_POLICY_DEFINED
            = "Extra vnf package change policy defined that is not associated with a vnf package change operation";
    public static final String ACTION_IS_MISSING_ASSOCIATION_WITH_ANY_VNF_PACKAGE_CHANGE_INTERFACE
            = "%s action is missing association with any vnf package change interface";
    public static final String WRONG_ROLLBACK_PATTERN_BASE = "Rollback pattern for %s is invalid: %s";
    public static final String WRONG_ROLLBACK_PATTERN_INSTALL_USAGE = "Install command has to follow delete or delete_pvc command";
    public static final String WRONG_ROLLBACK_PATTERN_ENDING = "Rollback pattern can not to be ended with upgrade command";
    public static final String WRONG_ROLLBACK_PATTERN_DELETE_USAGE = "%s can not be after delete or delete_pvc command";
    public static final String VDU_COMPUTE_NODES_NOT_ALLOWED_IN_REL4 = VDU_COMPUTE_NODE + " node is not allowed to be present in rel4 VNFD";
    public static final String SUBSTITUTION_MAPPINGS_REQUIREMENT_NOT_FOUND = "When VduCps are present there have to be substitution mapping section"
            + " in the VNFD specifying relationship between connection point and external virtual link";
    public static final String VDU_CP_MISSED_IN_SUBSTITUTION_MAPPINGS = "VduCp(s) %s were missed in substitution mappings section in VNFD";
    public static final String VDU_CP_WITH_UNSUPPORTED_VIRTUAL_LINK = "No virtual_links should be present for VduCp(s): %s as requirement since "
            + "internal links are not supported. Relationship between VduCp and virtual link must be modeled as substitution mapping";
    public static final String VDU_MISSED_IN_HELM_PARAMS_MAPPINGS = "Vdu(s) %s were defined for VduCps but don't presented in Helm params "
            + "mapping policies in VNFD";
    public static final String VIRTUAL_CP_MISSED_IN_HELM_PARAMS_MAPPINGS = "VirtualCp(s): %s were defined but don't presented in Helm "
            + "params mapping policies in VNFD";
    public static final String VIRTUAL_CP_MISSED_IN_SUBSTITUTION_MAPPINGS = "VirtualCp(s) %s were missed in substitution mappings section in VNFD";
    public static final String MCIOP_NAME = "Mciop-Name";
    public static final String AUTO_SCALING_MINREPLICAS_NAME = "Auto-Scaling-MinReplicas-Name";
    public static final String AUTO_SCALING_MAXREPLICAS_NAME = "Auto-Scaling-MaxReplicas-Name";
    public static final String SCALING_PARAMETER_NAME = "Scaling-Parameter-Name";
    public static final String AUTO_SCALING_ENABLED = "Auto-Scaling-Enabled";
    public static final String INVALID_VNFD_JSON_ERROR_MESSAGE = "Invalid vnfd json present in package, Failed due to %s";
    public static final String VNFLCM = "Vnflcm";
    public static final String FILE = "file";
    public static final String ASSOCIATED_VDU_KEY = "associatedVdu";
    public static final String VNFD_DOES_NOT_SUPPORT_OPERATION
            = "VNFD does not support operation %s. Node type interfaces are invalid";
    public static final String ADDITIONAL_PARAMETERS_TYPE_VALUE_MUST_BE_OF_TYPE_STRING
            = "Additional parameters type key must be of type String error message. %s";
    public static final String DESTINATION_DESCRIPTOR_ID_MUST_NOT_BE_EMPTY
            = "Destination descriptor id for operation type rollback must not be empty";
    public static final String UNKNOWN_OPERATION = "Unknown operation %s";

    public static final String VALIDATION_OF_EMPTY_DEPLOYABLE_MODULE_NAME_HAS_FAILED =
            "Validation failed due to empty name parameter in deployable module, this values could not be empty or null";
    public static final String VALIDATION_OF_DEPLOYABLE_MODULE_RELATION_HAS_FAILED_FORMAT =
            "Error due to inconsistent relation between deployable_modules in AttributesExtensions and their declaration in VNFD; this module: "
                    + "%s  is not present in node_template";
    public static final String VALIDATION_OF_DEPLOYABLE_MODULE_ASSOCIATED_ARTIFACTS_HAS_FAILED_FORMAT =
            "Error due to incorrect reference to chart: %s in associatedArtifacts of deployable_module";
    public static final String VALIDATION_OF_DEPLOYABLE_MODULES_FOR_DUPLICATES_FORMAT =
            "Error: deployable modules have duplicated values. Modules with names: %s, and same associatedArtifacts: %s";
    public static final String VALIDATION_OF_DEPLOYABLE_MODULES_FOR_NAMES_DUPLICATES_FORMAT =
            "Error: deployable modules have duplicated values. Modules with names: %s";
    public static final String VALIDATION_OF_DEPLOYABLE_MODULES_FOR_ASSOCIATED_ARTIFACTS_DUPLICATES_FORMAT =
            "Error: deployable modules with same associatedArtifacts were found: %s";
    public static final String VALIDATION_OF_OPTIONALITY_OF_DEPLOYABLE_MODULE_HAS_FAILED =
            "Error: all Deployable Modules which are present in topology_template should be declared in Extensions section either.";

    public static final String ASSOCIATED_ARTIFACTS_ARE_NULL_OR_MISSING = "associatedArtifacts of deployable_module are null or missing";
    public static final String VALIDATION_OF_LCM_OPERATION_HAS_FAILED = "Validation of %s operation has failed";
    public static final String PROPERTY_SHOULD_BE_A_MAP = "%s property should be a map";
    public static final String PROPERTY_NOT_FOUND_IN_VNFD = "Property %s was not found in VNFD";
    public static final String PROPERTIES_IN_DEPLOYABLE_MODULE_ARE_MISSING = "Properties for tosca.nodes.nfv.DeployableModule are missing";
    public static final List<String> EXTENSION_KEYS_THAT_SHOULD_BE_OF_TYPE_MAP = List.of(VNF_CONTROLLED_SCALING_PROPERTY,
                                                                                         DEPLOYABLE_MODULES_PROPERTY);
    public static final String DECRYPTION_CACHE_NAME = "decryptionCache";
    public static final String CAFFEINE_DECRYPTION_CACHE_NAME = "caffeineDecryptionCache";
    public static final String DECRYPTION_CACHE_MANAGER_NAME = "decryptionCacheManager";

    private Constants() {
    }
}
