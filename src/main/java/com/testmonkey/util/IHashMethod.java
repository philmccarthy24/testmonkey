package com.testmonkey.util;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface IHashMethod {
	public String getFileHash(String fileName) throws NoSuchAlgorithmException, IOException;
}
