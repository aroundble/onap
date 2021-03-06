/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.openecomp.activityspec.mocks;

import org.openecomp.activityspec.be.dao.ActivitySpecDao;
import org.openecomp.activityspec.be.dao.types.ActivitySpecEntity;

public class ActivitySpecDaoMock implements ActivitySpecDao {
  public ActivitySpecEntity activitySpec;

  @Override
  public void create(ActivitySpecEntity activitySpecEntity) {
    activitySpec = activitySpecEntity;
  }

  @Override
  public ActivitySpecEntity get(ActivitySpecEntity activitySpecEntity) {
    return activitySpec;
  }

  @Override
  public void update(ActivitySpecEntity activitySpecEntity) {
    activitySpec = activitySpecEntity;
  }
}
