package rest.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Service
public class HospitalAppointmentService {
    private ArrayList<Appointment> appointmentList = new ArrayList<>();

    public int nextId() {
        return appointmentList.isEmpty()
                ? 1
                : appointmentList.stream()
                    .mapToInt(x -> x.getId())
                    .max().getAsInt() + 1;
    }

    public Appointment lastAppointment() {
        return appointmentList.get(appointmentList.size() - 1);
    }

    public boolean isAppointmentExist(int idx) {
        return  !this.getAppointmentList().isEmpty()
                && (idx == -1 || this.getAppointmentById(idx).size() > 0);
    }

    public boolean isValidAppointment(Appointment appointment) {
        if (appointment.getDoctor() == null
                || appointment.getDoctor().trim().isEmpty()) { return false; }
        if (appointment.getPatient() == null
                || appointment.getPatient().trim().isEmpty()) { return false; }
        if (appointment.getDate() == null
                || appointment.getDate().trim().isEmpty()
                || !appointment.getDate().matches("\\d\\d\\d\\d-\\d\\d-\\d\\d")) { return false; }
        return true;
    }

    public Appointment formatAppoinment(Appointment appointment) {
        appointment.setId(nextId());
        appointment.setDoctor(appointment.getDoctor().toLowerCase());
        appointment.setPatient(appointment.getPatient().toLowerCase());
        return appointment;
    }

    @JsonIgnore
    private List<Appointment> getAppointmentById(int idx) {
        return this.getAppointmentList().stream().filter(x -> x.getId() == idx).toList();
    }

    public Appointment deleteAppointment(int idx) {
        if (idx == -1) {
            return this.getAppointmentList().remove(0);
        } else {
            int id = -1;
            for (int i = 0; i < this.getAppointmentList().size(); i++) {
                if (this.getAppointmentList().get(i).getId() == idx) {
                    id = i;
                    break;
                }
            }
            return id == -1
                    ? null
                    : this.getAppointmentList().remove(id);
        }
    }

    public Appointment deleteAppointments() {
        if (this.getAppointmentList().isEmpty()) {
            return new Appointment();
        } else {
            List<Appointment> resultList = List.copyOf(this.getAppointmentList());
            this.getAppointmentList().clear();
            return resultList.get(0);
        }
    }
}
