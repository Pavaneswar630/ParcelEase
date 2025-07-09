<?php
session_start();
header("Content-Type: application/json");
include "../dbconnect.php";

// Ensure driver is logged in
if (!isset($_SESSION['driver_id'])) {
    echo json_encode(["status" => "error", "message" => "Unauthorized"]);
    exit;
}

$driver_id = $_SESSION['driver_id'];
$status = $_POST['status'] ?? '';

// Validate status input
$valid_statuses = ['Available', 'Not Available'];
if (!in_array($status, $valid_statuses)) {
    echo json_encode(["status" => "error", "message" => "Invalid status"]);
    exit;
}

// Update driver status
$query = "UPDATE drivers SET driver_status = ? WHERE driver_id = ?";
$stmt = $conn->prepare($query);
$stmt->bind_param("si", $status, $driver_id);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Status updated successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to update status"]);
}

$stmt->close();
$conn->close();
?>
