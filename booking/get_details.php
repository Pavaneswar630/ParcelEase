<?php
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] != 'POST') {
    echo json_encode(['error' => 'Only POST method allowed']);
    exit;
}

include '../dbconnect.php';

$parcel_id = $_POST['parcel_id'] ?? '';

if (empty($parcel_id)) {
    echo json_encode(['error' => 'Parcel ID is required']);
    exit;
}

$response = [];

// Get parcel details
$parcel_sql = "SELECT * FROM parcels WHERE parcel_id = ?";
$stmt = $conn->prepare($parcel_sql);
$stmt->bind_param("s", $parcel_id);

$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(['error' => 'Parcel not found']);
    exit;
}

$parcel = $result->fetch_assoc();
$response['parcel'] = $parcel;

// Sender details
$response['sender'] = [
    'name' => $parcel['sender_name'],
    'phone' => $parcel['sender_phone'],
    'address' => $parcel['pickup_location']
];

// Receiver details
$response['receiver'] = [
    'name' => $parcel['receiver_name'],
    'phone' => $parcel['receiver_phone'],
    'address' => $parcel['drop_location']
];

// Get status from tracking table
$status_sql = "SELECT status FROM tracking WHERE parcel_id = ? ORDER BY id DESC LIMIT 1";
$status_stmt = $conn->prepare($status_sql);
$status_stmt->bind_param("s", $parcel_id); // ✅ Correct one
$status_stmt->execute();
$status_result = $status_stmt->get_result();

if ($status_row = $status_result->fetch_assoc()) {
    $response['status'] = $status_row['status'];
} else {
    $response['status'] = 'Unknown';
}

// Get timeline history
$timeline_sql = "SELECT status, location, timestamp FROM parcel_timeline WHERE parcel_id = ? ORDER BY timestamp DESC";
$timeline_stmt = $conn->prepare($timeline_sql);
$timeline_stmt->bind_param("s", $parcel_id);
$timeline_stmt->execute();
$timeline_result = $timeline_stmt->get_result();

$timeline = [];
while ($row = $timeline_result->fetch_assoc()) {
    $timeline[] = $row;
}
$response['timeline'] = $timeline;

echo json_encode($response);
?>
