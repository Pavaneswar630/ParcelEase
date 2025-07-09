<?php
header("Content-Type: application/json");
include "../../dbconnect.php";
session_start();

// Check if driver is logged in
if (!isset($_SESSION['driver_id'])) {
    echo json_encode(["status" => "error", "message" => "Driver not logged in"]);
    exit;
}

$driver_id = $_SESSION['driver_id'];

// Validate POST data
if (!isset($_POST['parcel_id'], $_POST['status'], $_POST['current_location'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$parcel_id = $_POST['parcel_id'];
$status = $_POST['status'];
$current_location = $_POST['current_location'];

// Allowed status values
$allowed_status = ['Pending','Pickup','In Transit','Out for Delivery','Delivered'];

if (!in_array($status, $allowed_status)) {
    echo json_encode(["status" => "error", "message" => "Invalid status value"]);
    exit;
}

// Check if the parcel is assigned to this driver
$queryCheck = "SELECT driver_id FROM parcels WHERE parcel_id = ? AND driver_id = ?";
$stmtCheck = $conn->prepare($queryCheck);
$stmtCheck->bind_param("si", $parcel_id, $driver_id);
$stmtCheck->execute();
$stmtCheck->store_result();

if ($stmtCheck->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Parcel not assigned to this driver"]);
    exit;
}

// Update tracking status and current location
$queryUpdate = "UPDATE tracking SET status = ?, current_location = ?, previous_locations = 
                CONCAT(IFNULL(previous_locations, ''), ', ', ?) WHERE parcel_id = ?";
$stmtUpdate = $conn->prepare($queryUpdate);
$stmtUpdate->bind_param("ssss", $status, $current_location, $current_location, $parcel_id);

if ($stmtUpdate->execute()) {
    echo json_encode(["status" => "success", "message" => "Tracking status updated"]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to update tracking status"]);
}

// Close connections
$stmtCheck->close();
$stmtUpdate->close();
$conn->close();
?>
