package com.example.emergencynow.data.error

import androidx.annotation.StringRes
import com.example.emergencynow.R

enum class ErrorEnum(@StringRes val value: Int) {
    INVALID_CREDENTIALS(R.string.error_invalid_credentials),
    INVALID_VERIFICATION_CODE(R.string.error_invalid_verification_code),
    VERIFICATION_CODE_EXPIRED(R.string.error_verification_code_expired),
    VERIFICATION_CODE_NOT_FOUND(R.string.error_verification_code_not_found),
    USER_NOT_IN_STATE_ARCHIVE(R.string.error_user_not_in_archive),
    INVALID_REFRESH_TOKEN(R.string.error_invalid_refresh_token),
    REFRESH_TOKEN_EXPIRED(R.string.error_refresh_token_expired),
    TOKEN_GENERATION_FAILED(R.string.error_token_generation_failed),
    UNAUTHORIZED_ACCESS(R.string.error_unauthorized_access),
    EMAIL_SEND_FAILED(R.string.error_email_send_failed),
    SMS_SEND_FAILED(R.string.error_sms_send_failed),
    VERIFICATION_CODE_GENERATION_FAILED(R.string.error_verification_code_generation_failed),
    LOGOUT_FAILED(R.string.error_logout_failed),
    INVALID_LOGIN_METHOD(R.string.error_invalid_login_method),

    AMBULANCE_NOT_FOUND(R.string.error_ambulance_not_found),
    AMBULANCE_ALREADY_EXISTS(R.string.error_ambulance_already_exists),
    INVALID_LICENSE_PLATE(R.string.error_invalid_license_plate),
    DRIVER_NOT_FOUND(R.string.error_driver_not_found),
    DRIVER_ALREADY_ASSIGNED(R.string.error_driver_already_assigned),
    AMBULANCE_NOT_AVAILABLE(R.string.error_ambulance_not_available),
    NO_AVAILABLE_AMBULANCES(R.string.error_no_available_ambulances),
    LOCATION_UPDATE_FAILED(R.string.error_location_update_failed),
    INVALID_LOCATION(R.string.error_invalid_location),
    AMBULANCE_CREATION_FAILED(R.string.error_ambulance_creation_failed),
    AMBULANCE_UPDATE_FAILED(R.string.error_ambulance_update_failed),
    AMBULANCE_DELETE_FAILED(R.string.error_ambulance_delete_failed),
    DRIVER_ASSIGNMENT_FAILED(R.string.error_driver_assignment_failed),
    DRIVER_REMOVAL_FAILED(R.string.error_driver_removal_failed),
    DISTANCE_CALCULATION_FAILED(R.string.error_distance_calculation_failed),

    CONTACT_NOT_FOUND(R.string.error_contact_not_found),
    MAX_CONTACTS_REACHED(R.string.error_max_contacts_reached),
    CONTACT_CREATION_FAILED(R.string.error_contact_creation_failed),
    CONTACT_UPDATE_FAILED(R.string.error_contact_update_failed),
    CONTACT_DELETE_FAILED(R.string.error_contact_delete_failed),
    UNAUTHORIZED_CONTACT_ACCESS(R.string.error_unauthorized_contact_access),

    PROFILE_NOT_FOUND(R.string.error_profile_not_found),
    PROFILE_ALREADY_EXISTS(R.string.error_profile_already_exists),
    PROFILE_CREATION_FAILED(R.string.error_profile_creation_failed),
    PROFILE_UPDATE_FAILED(R.string.error_profile_update_failed),
    PROFILE_DELETE_FAILED(R.string.error_profile_delete_failed),
    INVALID_PROFILE_DATA(R.string.error_invalid_profile_data),
    USER_NOT_FOUND(R.string.error_user_not_found),

    USER_ALREADY_EXISTS(R.string.error_user_already_exists),
    USER_CREATION_FAILED(R.string.error_user_creation_failed),
    USER_UPDATE_FAILED(R.string.error_user_update_failed),
    USER_DELETE_FAILED(R.string.error_user_delete_failed),
    INVALID_USER_ID(R.string.error_invalid_user_id),
    REFRESH_TOKEN_UPDATE_FAILED(R.string.error_refresh_token_update_failed),
    STATE_ARCHIVE_NOT_FOUND(R.string.error_state_archive_not_found),

    STATE_ARCHIVE_ALREADY_EXISTS(R.string.error_state_archive_already_exists),
    STATE_ARCHIVE_CREATION_FAILED(R.string.error_state_archive_creation_failed),
    STATE_ARCHIVE_UPDATE_FAILED(R.string.error_state_archive_update_failed),
    STATE_ARCHIVE_DELETE_FAILED(R.string.error_state_archive_delete_failed),
    INVALID_EGN(R.string.error_invalid_egn),
    INVALID_EMAIL(R.string.error_invalid_email),
    INVALID_PHONE_NUMBER(R.string.error_invalid_phone_number),
    EGN_ALREADY_EXISTS(R.string.error_egn_already_exists),

    HOSPITAL_NOT_FOUND(R.string.error_hospital_not_found),
    HOSPITAL_ALREADY_EXISTS(R.string.error_hospital_already_exists),
    HOSPITAL_CREATION_FAILED(R.string.error_hospital_creation_failed),
    HOSPITAL_UPDATE_FAILED(R.string.error_hospital_update_failed),
    HOSPITAL_DELETE_FAILED(R.string.error_hospital_delete_failed),
    NO_HOSPITALS_FOUND(R.string.error_no_hospitals_found),
    GOOGLE_PLACES_SYNC_FAILED(R.string.error_google_places_sync_failed),
    INVALID_HOSPITAL_DATA(R.string.error_invalid_hospital_data),

    CALL_NOT_FOUND(R.string.error_call_not_found),
    CALL_CREATION_FAILED(R.string.error_call_creation_failed),
    CALL_UPDATE_FAILED(R.string.error_call_update_failed),
    CALL_DELETE_FAILED(R.string.error_call_delete_failed),
    INVALID_CALL_STATUS(R.string.error_invalid_call_status),
    CALL_ALREADY_COMPLETED(R.string.error_call_already_completed),
    CALL_ALREADY_CANCELLED(R.string.error_call_already_cancelled),
    NO_AMBULANCE_ASSIGNED(R.string.error_no_ambulance_assigned),
    AMBULANCE_DISPATCH_FAILED(R.string.error_ambulance_dispatch_failed),
    HOSPITAL_NOT_SELECTED(R.string.error_hospital_not_selected),
    HOSPITAL_SELECTION_FAILED(R.string.error_hospital_selection_failed),
    ROUTE_CALCULATION_FAILED(R.string.error_route_calculation_failed),
    CONTACT_NOTIFICATION_FAILED(R.string.error_contact_notification_failed),
    DRIVER_RESPONSE_FAILED(R.string.error_driver_response_failed),
    NO_AVAILABLE_DRIVERS(R.string.error_no_available_drivers),
    INVALID_USER(R.string.error_invalid_user),

    INVALID_CONTACT_DATA(R.string.error_invalid_contact_data),

    DATABASE_ERROR(R.string.error_database),
}

fun getErrorMessage(errorCode: String?): Int {
    return try {
        if (errorCode != null) {
            val errorEnum = ErrorEnum.valueOf(errorCode)
            errorEnum.value
        } else {
            R.string.error_default
        }
    } catch (e: IllegalArgumentException) {
        R.string.error_default
    }
}
