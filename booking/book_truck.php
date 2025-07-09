<?php
include '../dbconnect.php';  // Your database connection file

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $user_id = $_POST['user_id'];
    $pickup_location = $_POST['pickup_location'];
    $pickup_lat = $_POST['pickup_lat'];
    $pickup_lng = $_POST['pickup_lng'];
    $drop_location = $_POST['drop_location'];
    $drop_lat = $_POST['drop_lat'];
    $drop_lng = $_POST['drop_lng'];
    $total_load = $_POST['total_load'];
    $purpose = $_POST['purpose'];
    $additional_info = isset($_POST['additional_info']) ? $_POST['additional_info'] : null;
    $name = $_POST['name'];
    $phone = $_POST['phone'];

    // Validate required fields
    if (empty($user_id) || empty($pickup_location) || empty($drop_location) || empty($total_load) || empty($purpose) || empty($name) || empty($phone)) {
        echo json_encode(["status" => "error", "message" => "All fields are required."]);
        exit();
    }

    // Insert into database
    $stmt = $conn->prepare("INSERT INTO truck_bookings (user_id, pickup_location, pickup_lat, pickup_lng, drop_location, drop_lat, drop_lng, total_load, purpose, additional_info, name, phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("issdddsdssss", $user_id, $pickup_location, $pickup_lat, $pickup_lng, $drop_location, $drop_lat, $drop_lng, $total_load, $purpose, $additional_info, $name, $phone);

    if ($stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Booking successful!", "booking_id" => $stmt->insert_id]);
    } else {
        echo json_encode(["status" => "error", "message" => "Booking failed!"]);
    }

    $stmt->close();
    $conn->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method."]);
}
?>
