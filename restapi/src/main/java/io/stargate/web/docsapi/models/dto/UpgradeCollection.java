/*
 * Copyright The Stargate Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.stargate.web.docsapi.models.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.stargate.web.docsapi.models.CollectionUpgradeType;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;

/** The DTO for the collection upgrade. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpgradeCollection {

  @JsonProperty("upgradeType")
  @NotNull(message = "`upgradeType` is required to upgrade a collection")
  private final CollectionUpgradeType upgradeType;

  @JsonCreator
  public UpgradeCollection(@JsonProperty("upgradeType") CollectionUpgradeType upgradeType) {
    this.upgradeType = upgradeType;
  }

  @ApiModelProperty(required = true, value = "The upgrade type, if an upgrade is available.")
  @JsonProperty("upgradeType")
  public CollectionUpgradeType getUpgradeType() {
    return upgradeType;
  }

  @Override
  public String toString() {
    return String.format("UpgradeCollectionDto(upgradeType=%s)", upgradeType);
  }
}