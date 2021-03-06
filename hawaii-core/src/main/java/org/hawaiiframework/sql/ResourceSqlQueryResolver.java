/*
 * Copyright 2015-2016 the original author or authors.
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

package org.hawaiiframework.sql;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;

import org.hawaiiframework.exception.HawaiiException;
import org.springframework.core.Ordered;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Simple implementation of {@link SqlQueryResolver} resolving sql queries using Spring's generic
 * {@link ResourceLoader} mechanism.
 *
 * @author Marcel Overdijk
 * @since 2.0.0
 */
public class ResourceSqlQueryResolver extends AbstractCachingSqlQueryResolver implements Ordered {

    /**
     * Default charset for retrieving sql query resources: UTF_8
     */
    public static final Charset DEFAULT_CHARSET = UTF_8;

    private final ResourceLoader resourceLoader;

    private Charset charset = DEFAULT_CHARSET;

    private String prefix = "";
    private String suffix = "";

    private int order = Ordered.LOWEST_PRECEDENCE;

    public ResourceSqlQueryResolver() {
        this(new DefaultResourceLoader());
    }

    public ResourceSqlQueryResolver(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Return the {@code Charset} for retrieving sql query resources.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Set the {@code Charset} for retrieving sql query resources.
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Return the prefix that gets prepended to sql query names when building the resource location.
     */
    protected String getPrefix() {
        return this.prefix;
    }

    /**
     * Set the prefix that gets prepended to sql query names when building the resource location.
     */
    public void setPrefix(String prefix) {
        this.prefix = (prefix != null ? prefix : "");
    }

    /**
     * Return the suffix that gets appended to sql query names when building the resource location.
     */
    protected String getSuffix() {
        return this.suffix;
    }

    /**
     * Set the suffix that gets appended to sql query names when building the resource location.
     */
    public void setSuffix(String suffix) {
        this.suffix = (suffix != null ? suffix : "");
    }

    /**
     * Return the order in which this {@link SqlQueryResolver} is evaluated.
     */
    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * Set the order in which this {@link SqlQueryResolver} is evaluated.
     */
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    protected String loadSqlQuery(String sqlQueryName) throws HawaiiException {
        String location = getPrefix() + sqlQueryName + getSuffix();
        Resource resource = this.resourceLoader.getResource(location);
        if (!resource.exists()) {
            return null;
        } else {
            try {
                return new Scanner(resource.getInputStream(), this.charset.name())
                        .useDelimiter("\\Z").next();
            } catch (IOException e) {
                throw new HawaiiException("Error reading resource: " + location, e);
            }
        }
    }
}
