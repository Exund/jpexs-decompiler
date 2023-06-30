/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.amf.amf3.NoSerializerExistsException;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Same as PlaceObject3Tag except additional AMF data
 *
 * @author JPEXS
 */
@SWFVersion(from = 19)
public class PlaceObject4Tag extends PlaceObject3Tag {

    public static final int ID = 94;

    public static final String NAME = "PlaceObject4";

    public Amf3Value amfData;

    /**
     * Constructor
     *
     * @param swf
     */
    public PlaceObject4Tag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    public PlaceObject4Tag(SWF swf, boolean placeFlagMove, int depth, String className, int characterId, MATRIX matrix, CXFORMWITHALPHA colorTransform, int ratio, String name, int clipDepth, List<FILTER> surfaceFilterList, int blendMode, Integer bitmapCache, Integer visible, RGBA backgroundColor, CLIPACTIONS clipActions, Amf3Value amfData, boolean placeFlagHasImage) {
        this(swf);
        init(swf, placeFlagMove, depth, className, characterId, matrix, colorTransform,ratio, name, clipDepth, surfaceFilterList, blendMode, bitmapCache, visible == null ? 0 : visible, backgroundColor, clipActions, placeFlagHasImage);
        this.amfData = amfData;
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public PlaceObject4Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        super.readData(sis, data, level, parallel, skipUnusualTags, lazy);
        if (sis.available() > 0) {
            try {
                amfData = sis.readAmf3Object("amfValue");
            } catch (NoSerializerExistsException nse) {
                amfData = new Amf3Value(nse.getIncompleteData());
                Logger.getLogger(PlaceObject4Tag.class.getName()).log(Level.WARNING, "AMFData in PlaceObject4 contains IExternalizable object which cannot be read. Data object is truncated.", nse);
            }
        }
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        super.getData(sos);
        if (amfData != null && amfData.getValue() != null) {
            try {
                sos.writeAmf3Object(amfData);
            } catch (NoSerializerExistsException ex) {
                throw new IOException("Class \"" + ex.getClassName() + "\" implements IExternalizable, it cannot be saved");
            }
        }
    }

    @Override
    public int getPlaceObjectNum() {
        return 4;
    }

    @Override
    public Amf3Value getAmfData() {
        return amfData;
    }
}
