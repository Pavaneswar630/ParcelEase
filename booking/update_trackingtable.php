<?php
header("Content-Type: application/json");
include "../dbconnect.php"; // Ensure database connection

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
    exit;
}

// Fetch all confirmed parcels with confirmed payment
$query = "SELECT p.parcel_id, p.pickup_location 
          FROM parcels p 
          JOIN payments py ON p.parcel_id = py.parcel_id 
          WHERE p.status = 'Confirmed' AND py.payment_status = 'Confirmed'";

$result = $conn->query($query);

if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $parcel_id = $row['parcel_id'];
        $current_location = $row['pickup_location'];

        // Check if the parcel already exists in the tracking table
        $checkQuery = "SELECT parcel_id FROM tracking WHERE parcel_id = ?";
        $stmtCheck = $conn->prepare($checkQuery);
        $stmtCheck->bind_param("s", $parcel_id);
        $stmtCheck->execute();
        $stmtCheck->store_result();

        if ($stmtCheck->num_rows == 0) { // If not in tracking table, insert it
            $insertQuery = "INSERT INTO tracking (parcel_id, status, current_location) 
                            VALUES (?, 'Pending', ?)";
            $stmtInsert = $conn->prepare($insertQuery);
            $stmtInsert->bind_param("ss", $parcel_id, $current_location);

            if ($stmtInsert->execute()) {
                echo json_encode(["status" => "success", "message" => "Tracking record added for Parcel ID: $parcel_id"]);
            } else {
                echo json_encode(["status" => "error", "message" => "Failed to insert tracking record"]);
            }
            $stmtInsert->close();
        }
        $stmtCheck->close();
    }
} else {
    echo json_encode(["status" => "error", "message" => "No confirmed parcels with confirmed payments"]);
}

$conn->close();
?>
