/*
 * Copyright 2020 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.registry.rules.compatibility;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Ales Justin
 */
public class ProtobufCompatibilityChecker implements CompatibilityChecker {

    /**
     * @see io.apicurio.registry.rules.compatibility.CompatibilityChecker#isCompatibleWith(io.apicurio.registry.rules.compatibility.CompatibilityLevel, java.util.List, java.lang.String)
     */
    @Override
    public boolean isCompatibleWith(CompatibilityLevel compatibilityLevel, List<String> existingSchemas, String proposedSchema) {
        requireNonNull(compatibilityLevel, "compatibilityLevel MUST NOT be null");
        requireNonNull(existingSchemas, "existingSchemas MUST NOT be null");
        requireNonNull(proposedSchema, "proposedSchema MUST NOT be null");

        if (existingSchemas.isEmpty()) {
            return true;
        }
        switch (compatibilityLevel) {
            case BACKWARD: {
                ProtobufFile fileBefore = new ProtobufFile(existingSchemas.get(existingSchemas.size() - 1));
                ProtobufFile fileAfter = new ProtobufFile(proposedSchema);
                ProtobufCompatibilityCheckerImpl checker = new ProtobufCompatibilityCheckerImpl(fileBefore, fileAfter);
                return checker.validate();
            }
            case BACKWARD_TRANSITIVE:
                ProtobufFile fileAfter = new ProtobufFile(proposedSchema);
                for (String existing : existingSchemas) {
                    ProtobufFile fileBefore = new ProtobufFile(existing);
                    ProtobufCompatibilityCheckerImpl checker = new ProtobufCompatibilityCheckerImpl(fileBefore, fileAfter);
                    if (!checker.validate()) {
                        return false;
                    }
                }
                return true;
            case FORWARD:
            case FORWARD_TRANSITIVE:
            case FULL:
            case FULL_TRANSITIVE:
                throw new IllegalStateException("Compatibility level " + compatibilityLevel + " not supported for Protobuf schemas");
            default:
                return true;
        }
    }
}
