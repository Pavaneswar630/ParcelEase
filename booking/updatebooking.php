<?php
session_start(); // Start the session
include "../dbconnect.php";
header("Content-Type: application/json");

// Check if user is logged in
if (!isset($_SESSION['user_id'])) {
    echo json_encode(["status" => "error", "message" => "User not logged in"]);
    exit;
}

$user_id = $_SESSION['user_id'];

// Check if request method is POST
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
    exit;
}

// Check if parcel_id is provided
if (!isset($_POST['parcel_id'])) {
    echo json_encode(["status" => "error", "message" => "Parcel ID is required"]);
    exit;
}

$parcel_id = $_POST['parcel_id'];

// Fetch parcel details to check ownership
$sql_check_parcel = "SELECT user_id FROM parcels WHERE parcel_id = ?";
$stmt_check_parcel = $conn->prepare($sql_check_parcel);
$stmt_check_parcel->bind_param("s", $parcel_id);
$stmt_check_parcel->execute();
$result_parcel = $stmt_check_parcel->get_result();
$parcel = $result_parcel->fetch_assoc();
$stmt_check_parcel->close();

if (!$parcel) {
    echo json_encode(["status" => "error", "message" => "Parcel not found"]);
    exit;
}

// Check if the logged-in user is the owner of the parcel
if ($parcel['user_id'] != $user_id) {
    echo json_encode(["status" => "error", "message" => "You are not authorized to update this parcel"]);
    exit;
}

// Check if parcel status in tracking table is 'Pickup' or 'Pending'
$sql_check_tracking = "SELECT status FROM tracking WHERE parcel_id = ?";
$stmt_check_tracking = $conn->prepare($sql_check_tracking);
$stmt_check_tracking->bind_param("s", $parcel_id);
$stmt_check_tracking->execute();
$result_tracking = $stmt_check_tracking->get_result();
$tracking = $result_tracking->fetch_assoc();
$stmt_check_tracking->close();

if (!$tracking || ($tracking['status'] !== 'Pickup' && $tracking['status'] !== 'Pending')) {
    echo json_encode(["status" => "error", "message" => "Parcel cannot be updated unless status is 'Pickup' or 'Pending'"]);
    exit;
}

// Prepare update fields dynamically
$updates = [];
$params = [];
$types = "";

// Build query based on provided fields
if (isset($_POST['pickup_location'])) {
    $updates[] = "pickup_location = ?";
    $params[] = $_POST['pickup_location'];
    $types .= "s";
}

if (isset($_POST['drop_location'])) {
    $updates[] = "drop_location = ?";
    $params[] = $_POST['drop_location'];
    $types .= "s";
}

if (isset($_POST['weight'])) {
    $updates[] = "weight = ?";
    $params[] = $_POST['weight'];
    $types .= "d";
}

if (isset($_POST['status'])) {
    $updates[] = "status = ?";
    $params[] = $_POST['status'];
    $types .= "s";
}

// If no fields were provided, exit
if (empty($updates)) {
    echo json_encode(["status" => "error", "message" => "No fields to update"]);
    exit;
}

// Prepare the update query
$query = "UPDATE parcels SET " . implode(", ", $updates) . " WHERE parcel_id = ?";
$params[] = $parcel_id;
$types .= "s";

$stmt = $conn->prepare($query);
$stmt->bind_param($types, ...$params);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Parcel updated successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to update parcel"]);
}

// Close statements and connection
$stmt->close();
$conn->close();
?>
