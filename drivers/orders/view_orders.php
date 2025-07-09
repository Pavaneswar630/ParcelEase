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

// Fetch all parcels assigned to the logged-in driver
$query = "SELECT 
            p.parcel_id, p.pickup_location, p.drop_location, p.weight, p.status AS parcel_status, p.created_at, 
            t.status AS tracking_status, t.current_location, t.eta, 
            u.id AS user_id, u.name AS customer_name, u.phone AS customer_phone 
          FROM parcels p
          LEFT JOIN tracking t ON p.parcel_id = t.parcel_id
          LEFT JOIN users u ON p.user_id = u.id
          WHERE p.driver_id = ?";

$stmt = $conn->prepare($query);
$stmt->bind_param("i", $driver_id);
$stmt->execute();
$result = $stmt->get_result();

$orders = [];
while ($row = $result->fetch_assoc()) {
    $orders[] = $row;
}

// Response
if (!empty($orders)) {
    echo json_encode(["status" => "success", "orders" => $orders]);
} else {
    echo json_encode(["status" => "error", "message" => "No orders found"]);
}

// Close
$stmt->close();
$conn->close();
?>
