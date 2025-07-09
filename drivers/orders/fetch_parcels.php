<?php
header("Content-Type: application/json");
include "../../dbconnect.php";

// Fetch all parcels with status 'Confirmed'
$query = "SELECT parcel_id, user_id, pickup_location, drop_location, weight, status, created_at 
          FROM parcels 
          WHERE status = 'Confirmed'";

$result = $conn->query($query);

$orders = [];
if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $orders[] = $row;
    }
    echo json_encode(["status" => "success", "orders" => $orders]);
} else {
    echo json_encode(["status" => "error", "message" => "No available orders"]);
}

$conn->close();
?>
