package com.scorpio4.assets;

import java.io.IOException;

/**
 * Scorpio4 (c) 2014
 * Module: com.scorpio4.assets
 * @author lee
 * Date  : 17/06/2014
 * Time  : 5:45 PM
 */
public abstract class BaseAssetRegister implements AssetRegister {

    @Override
    public Asset getAsset(String uri, String mimeType) throws IOException {
        return new Asset(uri, getString(uri, mimeType), mimeType);
    }

    public abstract String getString(String uri, String mimeType) throws IOException;

}
