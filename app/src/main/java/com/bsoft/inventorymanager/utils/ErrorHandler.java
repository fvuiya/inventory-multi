package com.bsoft.inventorymanager.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestoreException;

public class ErrorHandler {
    
    private static final String TAG = "ErrorHandler";
    
    /**
     * Handles general errors with appropriate logging and user feedback
     * @param context The context for showing toast
     * @param error The error that occurred
     * @param operationName The name of the operation that failed
     */
    public static void handleGeneralError(Context context, Throwable error, String operationName) {
        String errorMessage = "Error during " + operationName + ": " + 
                             (error != null ? error.getMessage() : "Unknown error");
        
        Log.e(TAG, errorMessage, error);
        
        // Provide user-friendly error message
        String userMessage = "An error occurred while " + operationName + ". Please try again.";
        if (error != null && error.getMessage() != null) {
            // Check for common error types and provide more specific messages
            String errorLower = error.getMessage().toLowerCase();
            if (errorLower.contains("network") || errorLower.contains("connection")) {
                userMessage = "Network connection error. Please check your internet connection and try again.";
            } else if (errorLower.contains("permission") || errorLower.contains("unauthorized")) {
                userMessage = "Access denied. You don't have permission to perform this operation.";
            } else if (errorLower.contains("timeout")) {
                userMessage = "Request timed out. Please check your connection and try again.";
            }
        }
        
        Toast.makeText(context, userMessage, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Handles Firestore-specific errors
     * @param context The context for showing toast
     * @param exception The Firestore exception
     * @param operationName The name of the operation that failed
     */
    public static void handleFirestoreError(Context context, FirebaseFirestoreException exception, String operationName) {
        if (exception == null) {
            handleGeneralError(context, new Exception("Unknown Firestore error"), operationName);
            return;
        }
        
        String errorMessage = "Firestore error during " + operationName + ": " + exception.getMessage();
        Log.e(TAG, errorMessage, exception);
        
        String userMessage;
        switch (exception.getCode()) {
            case ABORTED:
                userMessage = "Operation was aborted. Please try again.";
                break;
            case ALREADY_EXISTS:
                userMessage = "The item already exists.";
                break;
            case FAILED_PRECONDITION:
                userMessage = "The operation failed due to a failed precondition.";
                break;
            case INVALID_ARGUMENT:
                userMessage = "Invalid data provided. Please check your input.";
                break;
            case NOT_FOUND:
                userMessage = "The requested item was not found.";
                break;
            case PERMISSION_DENIED:
                userMessage = "Access denied. You don't have permission to perform this operation.";
                break;
            case RESOURCE_EXHAUSTED:
                userMessage = "Resource limit exceeded. Please try again later.";
                break;
            case UNAVAILABLE:
                userMessage = "Service unavailable. Please check your connection and try again.";
                break;
            case UNAUTHENTICATED:
                userMessage = "Authentication required. Please log in again.";
                break;
            default:
                userMessage = "An error occurred while " + operationName + ". Please try again.";
                break;
        }
        
        Toast.makeText(context, userMessage, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Handles validation errors
     * @param context The context for showing toast
     * @param fieldName The name of the field that failed validation
     * @param validationMessage The validation error message
     */
    public static void handleValidationError(Context context, String fieldName, String validationMessage) {
        String errorMessage = "Validation error for " + fieldName + ": " + validationMessage;
        Log.w(TAG, errorMessage);
        
        String userMessage = "Invalid " + fieldName + ": " + validationMessage;
        Toast.makeText(context, userMessage, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Handles network errors with fallback options
     * @param context The context for showing toast
     * @param error The network error
     * @param operationName The name of the operation that failed
     * @param fallbackAction Optional fallback action to take
     */
    public static void handleNetworkError(Context context, Throwable error, String operationName, Runnable fallbackAction) {
        String errorMessage = "Network error during " + operationName + ": " + 
                             (error != null ? error.getMessage() : "Unknown network error");
        
        Log.e(TAG, errorMessage, error);
        
        String userMessage = "Network error. Please check your connection and try again.";
        if (error != null && error.getMessage() != null) {
            String errorLower = error.getMessage().toLowerCase();
            if (errorLower.contains("timeout")) {
                userMessage = "Connection timed out. Please try again.";
            } else if (errorLower.contains("unreachable") || errorLower.contains("host")) {
                userMessage = "Unable to reach server. Please check your internet connection.";
            }
        }
        
        Toast.makeText(context, userMessage, Toast.LENGTH_LONG).show();
        
        // Execute fallback action if provided
        if (fallbackAction != null) {
            try {
                fallbackAction.run();
            } catch (Exception fallbackError) {
                Log.e(TAG, "Error executing fallback action", fallbackError);
            }
        }
    }
    
    /**
     * Logs security-related events
     * @param context The context for showing toast if needed
     * @param eventDescription Description of the security event
     */
    public static void logSecurityEvent(Context context, String eventDescription) {
        Log.w(TAG, "SECURITY EVENT: " + eventDescription);
        // In a production app, you might want to send this to a security monitoring system
    }
    
    /**
     * Handles data consistency errors
     * @param context The context for showing toast
     * @param operationName The name of the operation that failed
     * @param conflictDetails Details about the conflict
     */
    public static void handleDataConsistencyError(Context context, String operationName, String conflictDetails) {
        String errorMessage = "Data consistency error during " + operationName + ": " + conflictDetails;
        Log.e(TAG, errorMessage);
        
        String userMessage = "Data conflict detected. The data has been updated by another user. Please refresh and try again.";
        Toast.makeText(context, userMessage, Toast.LENGTH_LONG).show();
    }
}