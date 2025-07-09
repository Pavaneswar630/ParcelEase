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

// Count the number of parcels with status 'Delivered'
$query = "SELECT COUNT(*) AS total_deliveries FROM parcels 
          JOIN tracking ON parcels.parcel_id = tracking.parcel_id
          WHERE parcels.driver_id = ? AND tracking.status = 'Delivered'";

$stmt = $conn->prepare($query);
$stmt->bind_param("i", $driver_id);
$stmt->execute();
$result = $stmt->get_result();
$row = $result->fetch_assoc();

$total_deliveries = $row['total_deliveries'] ?? 0; // Default to 0 if no deliveries

echo json_encode(["status" => "success", "total_deliveries" => $total_deliveries]);

$stmt->close();
$conn->close();
?>
