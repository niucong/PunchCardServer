package com.niucong.yunshitu.config;

/**
 * Created by yunshitu on 17-12-22.
 */

public class GlobalConfiguration {
    private static Configuration mConfiguration;
    public static void setConfiguration(Configuration configuration) {
        mConfiguration = configuration;
    }
    public static Configuration getConfiguration() {
        return mConfiguration;
    }
}
