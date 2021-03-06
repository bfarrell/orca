/*
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.mahe.cleanup

import com.netflix.spinnaker.orca.batch.ExecutionListener
import com.netflix.spinnaker.orca.mahe.MaheService
import com.netflix.spinnaker.orca.pipeline.persistence.ExecutionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(0)
class FastPropertyCleanupListener extends ExecutionListener {

  private final MaheService mahe

  @Autowired
  FastPropertyCleanupListener(ExecutionRepository executionRepository, MaheService mahe) {
    super(executionRepository)
    this.mahe = mahe
  }

  @Override
  void afterExecution(String type, String id) {
    execution(type, id).with {
      context.propertyIdList.each {
        if (it.previous) {
          mahe.createProperty([property: it.previous])
        } else {
          mahe.deleteProperty(it.propertyId, "spinnaker rollback", extractEnvironment(it.propertyId))
        }
      }
    }
  }

  private String extractEnvironment(propertyId) {
    propertyId.find(~/\w+\|\w+\|(\w+)\|.*?/) { match, env ->
      env
    }
  }
}
