<?php
session_start();
include "../dbconnect.php";
header("Content-Type: application/json");

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
    exit;
}

if (
    !isset($_POST['user_id'], $_POST['pickup_location'], $_POST['drop_location'], $_POST['weight'],
            $_POST['sender_name'], $_POST['sender_phone'],
            $_POST['receiver_name'], $_POST['receiver_phone'],
            $_POST['package_type'], $_POST['amount'], $_POST['delivery_time'], $_POST['delivery_type'], $_POST['parcel_id'])
) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$user_id = $_POST['user_id'];
$pickup_location = $_POST['pickup_location'];
$drop_location = $_POST['drop_location'];
$weight = $_POST['weight'];
$package_type = $_POST['package_type'];
$amount = $_POST['amount'];
$delivery_time = $_POST['delivery_time'];
$sender_name = $_POST['sender_name'];
$sender_phone = $_POST['sender_phone'];
$receiver_name = $_POST['receiver_name'];
$receiver_phone = $_POST['receiver_phone'];
$delivery_type = $_POST['delivery_type'];
$distance = isset($_POST['distance_km']) ? $_POST['distance_km'] : 0;


$sender_remarks = isset($_POST['sender_remarks']) ? $_POST['sender_remarks'] : "";
$receiver_remarks = isset($_POST['receiver_remarks']) ? $_POST['receiver_remarks'] : "";

$parcel_id = $_POST['parcel_id'];

// Insert into parcels table
$sql = "INSERT INTO parcels 
(parcel_id, user_id, pickup_location, drop_location, weight, package_type, delivery_time, amount, status,
 sender_name, sender_phone, sender_remarks, 
 receiver_name, receiver_phone, receiver_remarks, deliverytype, distance) 
VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Pending', ?, ?, ?, ?, ?, ?, ?, ?)";

$stmt = $conn->prepare($sql);
$stmt->bind_param(
    "sissdssssssssssd",
    $parcel_id, $user_id, $pickup_location, $drop_location, $weight,
    $package_type, $delivery_time, $amount,
    $sender_name, $sender_phone, $sender_remarks,
    $receiver_name, $receiver_phone, $receiver_remarks,
    $delivery_type, $distance
);


if ($stmt->execute()) {
    // Insert into tracking table
    $status = 'Pending';
    $current_location = $pickup_location;
    $previous_locations = '';
    $eta = $delivery_time;

    $trackSql = "INSERT INTO tracking (parcel_id, status, current_location, previous_locations, eta, user_id)
                 VALUES (?, ?, ?, ?, ?, ?)";
    $trackStmt = $conn->prepare($trackSql);
    $trackStmt->bind_param("sssssi", $parcel_id, $status, $current_location, $previous_locations, $eta, $user_id);
    $trackStmt->execute();
    $trackStmt->close();

    // Store session
    $_SESSION['parcel_id'] = $parcel_id;
    $_SESSION['amount'] = $amount;

    echo json_encode([
        "status" => "success",
        "message" => "Parcel booked and tracking initialized.",
        "parcel_id" => $parcel_id,
        "amount" => $amount
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to create booking"]);
}

$stmt->close();
$conn->close();
?>
