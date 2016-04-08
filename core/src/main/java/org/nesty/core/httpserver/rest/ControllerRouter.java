package org.nesty.core.httpserver.rest;

import org.nesty.core.httpserver.rest.controller.URLController;

import java.util.HashMap;
import java.util.Map;

/**
 * Routeable Controller mapping
 *
 * Author : Michael
 * Date : March 07, 2016
 */
public class ControllerRouter {

    private final Map<URLResource, URLController> controller = new HashMap<>(256);

    public ControllerRouter() {
    }

    /**
     * get specified resource's handler or null if it don't exist
     *
     * @param resource url anaylzed resource
     * @return handler instance or null if it don't exist
     */
    public URLController findURLControlloer(URLResource resource) {
        return controller.get(resource);
    }

    public synchronized boolean put(URLResource resource, URLController handler) {
        return controller.put(resource, handler) == null;
    }
}