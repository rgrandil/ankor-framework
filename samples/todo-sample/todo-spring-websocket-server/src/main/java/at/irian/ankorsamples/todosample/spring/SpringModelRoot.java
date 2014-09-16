/*
 * Copyright (C) 2013-2014  Irian Solutions  (http://www.irian.at)
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

package at.irian.ankorsamples.todosample.spring;

import at.irian.ankor.ref.Ref;
import at.irian.ankor.viewmodel.factory.BeanFactories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 */
@Component
@Scope("prototype")
public class SpringModelRoot {

    @Autowired
    private Ref rootRef;

    private SpringTaskListModel model;

    @PostConstruct
    protected void init() {
        this.model = BeanFactories.newInstance(SpringTaskListModel.class, rootRef.appendPath("model"));
    }

    public SpringTaskListModel getModel() {
        return model;
    }

    public void setModel(SpringTaskListModel model) {
        this.model = model;
    }
}
