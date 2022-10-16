/*
 * *
 *  * Copyright (c) 2022, Janelia
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice,
 *  *    this list of conditions and the following disclaimer.
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.janelia.scicomp.v5.fs;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.GsonAttributesParser;
import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.saalfeldlab.n5.github.lib.LocalGitRepo;
import org.janelia.saalfeldlab.n5.zarr.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;

public class MultiVersionZarrReader extends N5ZarrReader {
    private final LocalGitRepo repo;

    public MultiVersionZarrReader(String basePath, RevCommit commit) throws IOException {
        super(basePath);
        this.repo = new LocalGitRepo(new File(basePath), commit);
    }

    protected InputStream readGitObject(String objectKey) throws IOException {
        ObjectLoader loader = repo.readFile(objectKey);
        return loader.openStream();
    }

    @Override
    public ZArrayAttributes getZArraryAttributes(String pathName) throws IOException {
        String path = Paths.get(removeLeadingSlash(pathName), ".zarray").toFile().getPath();
        HashMap<String, JsonElement> attributes = new HashMap();
        if (repo.exists(path)) {
            Throwable var5 = null;
            InputStream in = null;
            try {
                in = this.readGitObject(path);
                attributes.putAll(GsonAttributesParser.readAttributes(new InputStreamReader(in), this.gson));
            } catch (Throwable var14) {
                var5 = var14;
                throw var14;
            } finally {
                if (in != null) {
                    if (var5 != null) {
                        try {
                            in.close();
                        } catch (Throwable var13) {
                            var5.addSuppressed(var13);
                        }
                    } else {
                        in.close();
                    }
                }

            }
        } else {
            System.out.println(path + " does not exist.");
        }
        JsonElement sepElem = (JsonElement) attributes.get("dimension_separator");
        return new ZArrayAttributes(((JsonElement) attributes.get("zarr_format")).getAsInt(), (long[]) this.gson.fromJson((JsonElement) attributes.get("shape"), long[].class), (int[]) this.gson.fromJson((JsonElement) attributes.get("chunks"), int[].class), (DType) this.gson.fromJson((JsonElement) attributes.get("dtype"), DType.class), (ZarrCompressor) this.gson.fromJson((JsonElement) attributes.get("compressor"), ZarrCompressor.class), ((JsonElement) attributes.get("fill_value")).getAsString(), ((JsonElement) attributes.get("order")).getAsCharacter(), sepElem != null ? sepElem.getAsString() : this.dimensionSeparator, (Collection) this.gson.fromJson((JsonElement) attributes.get("filters"), TypeToken.getParameterized(Collection.class, new Type[]{Filter.class}).getType()));
    }

    @Override
    public DataBlock<?> readBlock(String pathName, DatasetAttributes datasetAttributes, long... gridPosition) throws IOException {
        ZarrDatasetAttributes zarrDatasetAttributes;
        if (datasetAttributes instanceof ZarrDatasetAttributes) {
            zarrDatasetAttributes = (ZarrDatasetAttributes) datasetAttributes;
        } else {
            zarrDatasetAttributes = this.getZArraryAttributes(pathName).getDatasetAttributes();
        }

        String attrDimensionSeparator = zarrDatasetAttributes.getDimensionSeparator();
        String dimSep;
        if (attrDimensionSeparator != null && !attrDimensionSeparator.isEmpty()) {
            dimSep = attrDimensionSeparator;
        } else {
            dimSep = this.dimensionSeparator;
        }

        String path = Paths.get(removeLeadingSlash(pathName), getZarrDataBlockPath(gridPosition, dimSep, zarrDatasetAttributes.isRowMajor()).toString()).toFile().getPath();

        if (!repo.exists(path)) {
            return null;
        } else {
            Throwable var9 = null;
            InputStream in = null;
            DataBlock var10;
            try {
                in = this.readGitObject(path);
                new InputStreamReader(in);
                var10 = readBlock(in, zarrDatasetAttributes, gridPosition);
            } catch (Throwable var19) {
                var9 = var19;
                throw var19;
            } finally {
                if (in != null) {
                    if (var9 != null) {
                        try {
                            in.close();
                        } catch (Throwable var18) {
                            var9.addSuppressed(var18);
                        }
                    } else {
                        in.close();
                    }
                }

            }

            return var10;
        }
    }
}
