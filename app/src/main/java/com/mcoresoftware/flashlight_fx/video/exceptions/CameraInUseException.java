package com.mcoresoftware.flashlight_fx.video.exceptions;

public class CameraInUseException extends RuntimeException {

    public CameraInUseException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -1866132102949435675L;
}
