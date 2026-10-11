package uz.unnarsx.cherrygram.camera;

import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig;

public final class CameraSettings {
    private CameraSettings() {}

    public static boolean takeFrontFacing() {
        return !CherrygramCameraConfig.INSTANCE.getRearCam();
    }

    public static int roundResolution(int fallback) {
        int resolution = CherrygramCameraConfig.INSTANCE.getVideoMessagesResolution();
        return resolution > 0 ? resolution : fallback;
    }

    public static void advanceFlashHint() {
        int count = CherrygramCameraConfig.INSTANCE.getVideoMessagesHintCount();
        CherrygramCameraConfig.INSTANCE.setVideoMessagesHintCount(Math.min(3, count + 1));
    }
}
