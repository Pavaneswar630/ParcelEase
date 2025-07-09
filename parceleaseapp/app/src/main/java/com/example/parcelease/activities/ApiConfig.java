package com.example.parcelease.activities;

public class ApiConfig {
    // ✅ ONLY update this one line when switching networks or ngrok
    public static final String BASE_URL = " https://54aa6321861c.ngrok-free.app/parcelease/";
    public static final String BASE_2URL = "http://192.168.1.14/parcelease/";

    public static String getSendOtpUrl() {
        return BASE_2URL + "user/send_otp.php";
    }

    public static String getRegisterUrl() {
        return BASE_URL + "user/register.php";
    }

    public static String getLoginUrl() {
        return BASE_URL + "user/login.php";
    }
    public static String getsaveaddressUrl() {
        return BASE_URL + "saved_address/save_address.php";
    }
    public static String getsaveaddressesUrl() {
        return BASE_URL + "saved_address/get_saved_addresses.php";
    }
    public static String getcreateparcelUrl() {
        return BASE_URL + "booking/createparcel.php";
    }
    public static String getforgotpassUrl() {
        return BASE_2URL + "user/forgotpass.php";
    }
    public static String get_ticketsUrl() {
        return BASE_URL + "support/get_tickets.php";
    }
    public static String getrecentactivitiesUrl() {
        return BASE_URL + "booking/recent_activities.php";
    }
    public static String getcreatebookingUrl() {
        return BASE_URL + "booking/createbooking.php";
    }
    public static String gethistoryUrl() {
        return BASE_URL + "booking/history.php";
    }
    public static String getticketsUrl() {
        return BASE_URL + "support/gettickets.php";
    }
    public static String getbookingUrl() {
        return BASE_URL + "booking/getbooking.php";
    }
    public static String getmakepaymentUrl() {
        return BASE_URL + "payment/makepayment.php";
    }
    public static String getprofileUrl() {
        return BASE_URL + "user/get_profile.php";
    }
    public static String getdetailsUrl() {
        return BASE_URL + "booking/get_details.php";
    }
    public static String getsubmitticketUrl() {
        return BASE_URL + "support/submitticket.php";
    }
    public static String getinvoiceUrl() {
        return BASE_URL + "booking/get_invoice.php";
    }
    public static String gettrackparcelUrl() {
        return BASE_URL + "tracking/trackparcel.php";
    }
    public static String getbooktruckUrl() {
        return BASE_URL + "booking/book_truck.php";
    }
    public static String getactivateaccountUrl() {
        return BASE_2URL + "user/activateaccount.php";
    }
    // Add more API endpoints as needed...
}

