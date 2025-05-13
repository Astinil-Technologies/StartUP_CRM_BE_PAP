package startup.backend.util;

import java.util.Base64;

public class Utility {

    public static String encodeImageToBase64(byte[] imageData) {
        return Base64.getEncoder().encodeToString(imageData);
    }

    public static byte[] decodeBase64ToImage(String base64Image) {
        if (base64Image.contains(",")) {
            base64Image = base64Image.split(",")[1];
        }
        return Base64.getDecoder().decode(base64Image);
    }


}
