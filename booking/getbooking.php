<?php
include '../dbconnect.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $type = $_POST['type'];
    $parcel_id = $_POST['parcel_id'] ?? null;
    $id = $_POST['id'] ?? null;

    if ($type == "standard" && $parcel_id) {
        $query = "SELECT parcel_id, pickup_location, drop_location, weight, status, created_at, 
                         sender_name, sender_phone, receiver_name, receiver_phone, 
                         package_type, delivery_time 
                  FROM parcels WHERE parcel_id = ?";
    } elseif ($type == "incity" && $parcel_id) {
        $query = "SELECT id, parcel_id, user_id, pickup_location, drop_location, weight, package_type, vehicle_type, 
                         delivery_time, amount, status, sender_name, sender_phone, receiver_name, receiver_phone, created_at 
                  FROM incitybookings WHERE parcel_id = ?";
    } elseif ($type == "truck" && $id) {
        $query = "SELECT id, user_id, pickup_location, pickup_lat, pickup_lng, drop_location, drop_lat, drop_lng, 
                         total_load, purpose, additional_info, name, phone, status, created_at 
                  FROM truckbooking WHERE id = ?";
    } else {
        echo json_encode(["success" => false, "message" => "Invalid request parameters"]);
        exit;
    }

    $stmt = $conn->prepare($query);
    if ($type == "truck") {
        $stmt->bind_param("s", $id);
    } else {
        $stmt->bind_param("s", $parcel_id);
    }
    
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        echo json_encode(["success" => true, "data" => $result->fetch_assoc()]);
    } else {
        echo json_encode(["success" => false, "message" => "No details found"]);
    }
}
?>
