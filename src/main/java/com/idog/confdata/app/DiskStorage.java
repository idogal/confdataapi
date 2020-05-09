package com.idog.confdata.app;

import java.io.IOException;

public interface DiskStorage {

    void persist(String key, String value);

    String get(String key);
}
