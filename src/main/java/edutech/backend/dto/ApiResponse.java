package edutech.backend.dto;


import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;


@Setter
@Accessors(chain = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor


public class ApiResponse<ResponseType> {
    /**
     * Status code of the response.
     */
private int code;

    /**
 * Indicates whether the API request was successful or not.
 */
private boolean success;

/**
 * A descriptive message providing details about the API request/response.
 */
private String message;

/**
 * Data related to the API response, of a generic type defined by {@code T}.
 */
private ResponseType data;


    /**
     * Static factory method to create an error response.
     *
     * @param message The error message to be included in the response.
     * @param code    The HTTP status code for the error.
     * @param <ResponseType> The type of the response data.
     * @return An {@link ApiResponse} representing an error, with no additional data.
     */

    public static <ResponseType> ApiResponse<ResponseType> error(String message, int code) {
        return new ApiResponse<>(code, false, message,  null);
    }


    /**
     * Static factory method to create a success response with data.
     *
     * @param message The success message to be included in the response.
     * @param data    The data to be included in the response.
     * @param code    The HTTP status code for the success.
     * @param <ResponseType> The type of the response data.
     * @return An {@link ApiResponse} representing success, with included data.
     */
    public static <ResponseType> ApiResponse<ResponseType> success(String message, ResponseType data, int code) {
        return new ApiResponse<>(code, true, message,  data);
    }
    /**
     * Static factory method to create a success response with a token.
     *
     * @param message The success message to be included in the response.
     * @param token   The token to be included in the response, typically for authentication.
     * @param code    The HTTP status code for the success.
     * @return An {@link ApiResponse} representing success, with an included token.
     */
    public static  <ResponseType> ApiResponse<ResponseType> successWithToken(String message, String token, int code,ResponseType data) {
        return new ApiResponse<>(code, true, message, data);
    }
    /**
     * Static factory method to create a success response with tokens in a map.
     * @param message The success message to be included in the response.
     * @param code    The HTTP status code for the success.
     * @return An {@link ApiResponse} representing success, with included tokens.
     */
    public static ApiResponse<Map<String, String>> successWithTokens(String message, Map<String, String> tokens, int code) {
        return new ApiResponse<>(code, true, message,  tokens);
    }
}