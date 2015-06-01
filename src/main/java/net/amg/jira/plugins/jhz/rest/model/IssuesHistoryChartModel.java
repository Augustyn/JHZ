/*
 * Copyright 2015 AMG.net - Politechnika Łódzka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.amg.jira.plugins.jhz.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author jarek
 */
@XmlRootElement
public class IssuesHistoryChartModel {


    @XmlElement
    private String location;
    @XmlElement
    private String filterTitle;
    @XmlElement
    private String imageMap;
    @XmlElement
    private String imageMapName;
    @XmlElement
    private int width;
    @XmlElement
    private int height;
    @XmlElement
    private Table table;

    public IssuesHistoryChartModel(String location, String filterTitle, String imageMap, String imageMapName, int width, int height) {
        this.location = location;
        this.filterTitle = filterTitle;
        this.imageMap = imageMap;
        this.imageMapName = imageMapName;
        this.width = width;
        this.height = height;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}
