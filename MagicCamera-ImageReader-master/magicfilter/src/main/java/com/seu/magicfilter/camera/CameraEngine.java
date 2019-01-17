package com.seu.magicfilter.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.view.SurfaceView;

import java.io.IOException;

import static com.seu.magicfilter.camera.utils.CameraUtils.adaptFpsRange;
import static com.seu.magicfilter.camera.utils.CameraUtils.adaptPictureSize;
import static com.seu.magicfilter.camera.utils.CameraUtils.adaptPreviewSize;

public class CameraEngine {
    public static final int RECORD_WIDTH = 480, RECORD_HEIGHT = 640;

    private static Camera camera = null;
    private static int cameraID = 0;
    private static SurfaceTexture surfaceTexture;
    private static SurfaceView surfaceView;

    public static Camera getCamera() {
        return camera;
    }

    public static boolean openCamera() {
        if (camera == null) {
            try {
                camera = Camera.open(cameraID);
                setDefaultParameters();
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean openCamera(int id) {
        if (camera == null) {
            try {
                camera = Camera.open(id);
                cameraID = id;
                setDefaultParameters();
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
        return false;
    }

    public static void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void resumeCamera() {
        openCamera();
    }

    public void setParameters(Parameters parameters) {
        camera.setParameters(parameters);
    }

    public Parameters getParameters() {
        if (camera != null)
            camera.getParameters();
        return null;
    }

    public static void switchCamera() {
        releaseCamera();
        cameraID = cameraID == 0 ? 1 : 0;
        openCamera(cameraID);
        startPreview(surfaceTexture);
    }

    private static void setDefaultParameters() {
        Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        //设置帧Fps区间
        int[] range = adaptFpsRange(24, parameters);
        parameters.setPreviewFpsRange(range[0], range[1]);

        //设置大小
        Size previewSize = adaptPreviewSize(parameters);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        Size pictureSize = adaptPictureSize(parameters);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);

        parameters.setRotation(90);
        camera.setParameters(parameters);
    }

    private static Size getPreviewSize() {
        return camera.getParameters().getPreviewSize();
    }

    private static Size getPictureSize() {
        return camera.getParameters().getPictureSize();
    }

    public static void startPreview(SurfaceTexture surfaceTexture) {
        if (camera != null)
            try {
                camera.setPreviewTexture(surfaceTexture);
                CameraEngine.surfaceTexture = surfaceTexture;
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void startPreview() {
        if (camera != null)
            camera.startPreview();
    }

    public static void stopPreview() {
        camera.stopPreview();
    }

    public static void setRotation(int rotation) {
        Camera.Parameters params = camera.getParameters();
        params.setRotation(rotation);
        camera.setParameters(params);
    }

    public static void takePicture(Camera.ShutterCallback shutterCallback, Camera.PictureCallback rawCallback,
                                   Camera.PictureCallback jpegCallback) {
        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    public static com.seu.magicfilter.camera.utils.CameraInfo getCameraInfo() {
        com.seu.magicfilter.camera.utils.CameraInfo info = new com.seu.magicfilter.camera.utils.CameraInfo();
        Size size = getPreviewSize();
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraID, cameraInfo);
        info.previewWidth = size.width;
        info.previewHeight = size.height;
        info.orientation = cameraInfo.orientation;
        info.isFront = cameraID == 1;
        size = getPictureSize();
        info.pictureWidth = size.width;
        info.pictureHeight = size.height;
        return info;
    }
}