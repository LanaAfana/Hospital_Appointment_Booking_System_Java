package rest.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.business.Appointment;
import rest.business.HospitalAppointmentService;

import javax.validation.constraints.Null;
import java.util.Map;
import java.util.Optional;


@RestController
public class HospitalAppointmentController {

    private HospitalAppointmentService appointmentService;

    @Autowired
    public HospitalAppointmentController(HospitalAppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/setAppointment")
    public ResponseEntity<?> postAppointment(@RequestBody Appointment appointment) {
        if (appointmentService.isValidAppointment(appointment)) {
            appointmentService.getAppointmentList().add(
                    appointmentService.formatAppoinment(appointment));
            return ResponseEntity
                    .ok(appointmentService.lastAppointment());
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments() throws JsonProcessingException {
        if (appointmentService.getAppointmentList().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body(null);
        } else {
            return ResponseEntity
                    .ok(new ObjectMapper()
                            .writeValueAsString(appointmentService.getAppointmentList()));
        }
    }

    @DeleteMapping("/deleteAppointment")
    public ResponseEntity<?> deleteAppointment(@RequestParam(required = false) Optional<Integer> idx) {
        int id = idx.isPresent() ? idx.get() : -1;
        return appointmentService.isAppointmentExist(id)
                ? ResponseEntity
                    .ok(appointmentService.deleteAppointment(id))
                    : ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "The appointment does not exist or was already cancelled"));
    }

}

