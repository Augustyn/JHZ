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

    public IssuesHistoryChartModel(String location, String filterTitle, String imageMap, String imageMapName, int width, int height) {
        this.location = location;
        this.filterTitle = filterTitle;
        this.imageMap = imageMap;
        this.imageMapName = imageMapName;
        this.width = width;
        this.height = height;
    }


}
