/**
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.repository.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.onehippo.repository.xml.ContentResourceLoader;


/**
 * {@link ContentResourceLoader} implementation for loading resources from a zip file.
 */
public class ZipFileContentResourceLoader implements ContentResourceLoader {

    private final File file;
    private final ZipFile zipFile;

    public ZipFileContentResourceLoader(final ZipFile zipFile) {
        this.file = new File(zipFile.getName());
        this.zipFile = zipFile;
    }

    /**
     * Returns a URL in jar URL scheme.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/URI_scheme">http://en.wikipedia.org/wiki/URI_scheme</a>
     * @see <a href="https://www.iana.org/assignments/uri-schemes/prov/jar">https://www.iana.org/assignments/uri-schemes/prov/jar</a>
     */
    @Override
    public URL getResource(String path) throws MalformedURLException {
        if (path == null) {
            throw new IllegalArgumentException("Path is null.");
        }

        final String relPath = path.startsWith("/") ? path.substring(1) : path;
        final ZipEntry entry = zipFile.getEntry(relPath);

        if (entry != null) {
            URI uri = URI.create("jar:" + file.toURI().toString() + "!/" + relPath);
            return uri.toURL();
        }

        return null;
    }

    @Override
    public InputStream getResourceAsStream(String path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path is null.");
        }

        final String relPath = path.startsWith("/") ? path.substring(1) : path;
        final ZipEntry entry = zipFile.getEntry(relPath);

        if (entry != null) {
            return zipFile.getInputStream(entry);
        }

        return null;
    }

}
