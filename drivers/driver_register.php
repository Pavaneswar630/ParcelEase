<?php
header("Content-Type: application/json");
include "../dbconnect.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
    exit;
}

$name = $_POST['name'] ?? '';
$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';
$phone = $_POST['phone'] ?? '';
$vehicle_number = $_POST['vehicle_number'] ?? '';
$license_number = $_POST['license_number'] ?? '';

if (empty($name) || empty($email) || empty($password) || empty($phone) || empty($vehicle_number) || empty($license_number)) {
    echo json_encode(["status" => "error", "message" => "All fields are required"]);
    exit;
}

// Check if email already exists
$check_query = "SELECT driver_id FROM drivers WHERE email = ?";
$stmt = $conn->prepare($check_query);
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Email already registered"]);
    exit;
}

// Default status = Pending
$status = 'Pending';
$query = "INSERT INTO drivers (name, email, password, phone, vehicle_number, license_number, status, created_at) 
          VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
$stmt = $conn->prepare($query);
$stmt->bind_param("sssssss", $name, $email, $password, $phone, $vehicle_number, $license_number, $status);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Driver registered successfully. Pending approval."]);
} else {
    echo json_encode(["status" => "error", "message" => "Registration failed"]);
}

$stmt->close();
$conn->close();
?>
