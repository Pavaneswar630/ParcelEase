<?php
include '../dbconnect.php';

$parcel_id = $_POST['parcel_id'];
$user_id =(string)$_POST['user_id'];
$pickup_location = $_POST['pickup_location'];
$drop_location = $_POST['drop_location'];
$weight = (string)$_POST['weight'];
$package_type = $_POST['package_type'];
$vehicle_type = $_POST['vehicle_type'];
$delivery_time = $_POST['delivery_time'];
$amount = (string)$_POST['amount'];
$sender_name = $_POST['sender_name'];
$sender_phone = $_POST['sender_phone'];
$receiver_name = $_POST['receiver_name'];
$receiver_phone = $_POST['receiver_phone'];

$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Insert into parcels table (all fields as VARCHAR)
$sql = "INSERT INTO incitybookings 
(parcel_id, user_id, pickup_location, drop_location, weight, package_type, vehicle_type, delivery_time, amount, status, sender_name, sender_phone, receiver_name, receiver_phone) 
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pending', ?, ?, ?, ?)";

// Prepare statement
$stmt = $conn->prepare($sql);

if (!$stmt) {
    die("Prepare failed: " . $conn->error);
}

// Bind parameters (all as strings)
$stmt->bind_param(
    "sssssssssssss",
    $parcel_id, $user_id, $pickup_location, $drop_location, $weight,
    $package_type, $vehicle_type, $delivery_time, $amount,
    $sender_name, $sender_phone, $receiver_name, $receiver_phone
);

// Execute the statement
if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Booking created successfully"]);
} else {
    echo json_encode(["success" => false, "error" => $stmt->error]);
}

// Close statement and connection
$stmt->close();
$conn->close();
?>
