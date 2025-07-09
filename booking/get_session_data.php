<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

include "../dbconnect.php"; // Make sure this file correctly connects to your database

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // ✅ Read user_id from form-data
    $user_id = $_POST['user_id'] ?? '';

    if (empty($user_id)) {
        echo json_encode(["status" => "error", "message" => "User ID missing"]);
        exit;
    }

    // ✅ Fetch latest unpaid booking for this user
    $stmt = $conn->prepare("SELECT parcel_id, amount FROM bookings WHERE user_id = ? AND payment_status = 'pending' LIMIT 1");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows > 0) {
        $booking = $result->fetch_assoc();
        echo json_encode([
            "status" => "success",
            "parcel_id" => $booking['parcel_id'],
            "amount" => $booking['amount']
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "No active booking found!"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>
