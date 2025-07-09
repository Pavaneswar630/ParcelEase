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

// Validate request method
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
    exit;
}

// Validate input
if (!isset($_POST['parcel_id'])) {
    echo json_encode(["status" => "error", "message" => "Parcel ID is required"]);
    exit;
}

$parcel_id = $_POST['parcel_id'];

// Check if the parcel exists and is available
$query = "SELECT status FROM parcels WHERE parcel_id = ? AND status = 'Confirmed'";
$stmt = $conn->prepare($query);
$stmt->bind_param("s", $parcel_id);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Parcel not available for pickup"]);
    exit;
}
$stmt->close();

// Assign driver and update parcel status
$updateParcelQuery = "UPDATE parcels SET status = 'Pickup', driver_id = ? WHERE parcel_id = ?";
$stmtUpdateParcel = $conn->prepare($updateParcelQuery);
$stmtUpdateParcel->bind_param("is", $driver_id, $parcel_id);

// Update tracking table status to "Pickup"
$updateTrackingQuery = "UPDATE tracking SET status = 'Pickup' WHERE parcel_id = ?";
$stmtUpdateTracking = $conn->prepare($updateTrackingQuery);
$stmtUpdateTracking->bind_param("s", $parcel_id);

if ($stmtUpdateParcel->execute() && $stmtUpdateTracking->execute()) {
    echo json_encode(["status" => "success", "message" => "Order accepted successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to accept order"]);
}

// Close statements and connection
$stmtUpdateParcel->close();
$stmtUpdateTracking->close();
$conn->close();
?>
