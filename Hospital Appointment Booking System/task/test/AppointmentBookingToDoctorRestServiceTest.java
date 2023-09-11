import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.hyperskill.hstest.common.JsonUtils.getJson;
import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.*;


class RequestForTest {


    private Map<String, Object> properties = new LinkedHashMap<>();

    public RequestForTest(RequestForTest another) {
        this.properties = another.properties.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public RequestForTest() {
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.writeValueAsString(this.properties);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
            return null;
        }

    }


    public RequestForTest setProps(String key, Object value) {
        properties.put(key, value);
        return this;
    }

}

public class AppointmentBookingToDoctorRestServiceTest extends SpringTest {
    private String convert(String[] trs) {
        JsonArray jsonArray = new JsonArray();
        for (String tr : trs) {
            JsonElement jsonObject = JsonParser.parseString(tr);
            jsonArray.add(jsonObject);
        }
        return jsonArray.toString();
    }

    //Endpoints list
    private final String setAppointment = "/setAppointment";
    private final String appointments = "/appointments";
    private final String deleteAppointment = "/deleteAppointment?id=";

    // Doctors' names
    private final String phillGood = "Phill good";
    private final String leaWong = "Lea Wong";
    private final String pamelaUpperson = "Pamela Upperson";
    private final String doctorHouse = "Dr. House";


    //List and maps for corrent information storage about doctors, appointments, available days, statistics
    List<Long> ids = new ArrayList<>();
    List<Long> idsForAppointments = new ArrayList<>();

    List<String> appointmentsCorrectJson = new ArrayList<>();


    LocalDateTime date = LocalDateTime.now();
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String leaWongApp1 = new RequestForTest().setProps("doctor", leaWong)
            .setProps("patient", "Ay Bolit")
            .setProps("date", dateTimeFormatter.format(date.plusDays(1))).toJson();

    private final String pamelaUppersonApp1 = new RequestForTest().setProps("doctor", "Pamela Upperson")
            .setProps("patient", "John Galt")
            .setProps("date", dateTimeFormatter.format(date.plusDays(1))).toJson();

    private final String leaWongApp2 = new RequestForTest().setProps("doctor", "Lea Wong")
            .setProps("patient", "Indi Grimes")
            .setProps("date", dateTimeFormatter.format(date.plusDays(2))).toJson();

    private final String leaWongApp3 = new RequestForTest().setProps("doctor", "Lea Wong")
            .setProps("patient", "Indi Grimes")
            .setProps("date", dateTimeFormatter.format(date.plusDays(3))).toJson();

    private final String leaWongApp4 = new RequestForTest().setProps("doctor", "Lea Wong")
            .setProps("patient", "Indi Grimes")
            .setProps("date", dateTimeFormatter.format(date.plusDays(4))).toJson();

    private final String pamelaUppersonApp2 = new RequestForTest().setProps("doctor", "Pamela Upperson")
            .setProps("patient", "Ay")
            .setProps("date", dateTimeFormatter.format(date.plusDays(2))).toJson();

    private final String pamelaUppersonApp3 = new RequestForTest().setProps("doctor", "Pamela Upperson")
            .setProps("patient", "Ay")
            .setProps("date", dateTimeFormatter.format(date.plusDays(3))).toJson();

    private final String pamelaUppersonApp4 = new RequestForTest().setProps("doctor", "Pamela Upperson")
            .setProps("patient", "Ay")
            .setProps("date", dateTimeFormatter.format(date.plusDays(4))).toJson();

    private final RequestForTest newDocLeaWong = new RequestForTest().setProps("doctor", leaWong)
            .setProps("patient", "Bol it")
            .setProps("date", dateTimeFormatter.format(date));

    private final String doctorNameEmpty = new RequestForTest(newDocLeaWong).setProps("doctor", "").toJson();
    private final String doctorNameSpaces = new RequestForTest(newDocLeaWong).setProps("doctor", "   ").toJson();
    private final String noPatientName = new RequestForTest(newDocLeaWong).setProps("patient", null).toJson();
    private final String patientNameEmpty = new RequestForTest(newDocLeaWong).setProps("patient", "").toJson();
    private final String patientSpaces = new RequestForTest(newDocLeaWong).setProps("patient", "      ").toJson();
    private final String noDoctorName = new RequestForTest(newDocLeaWong).setProps("doctor", null).toJson();
    private final String dateEmpty = new RequestForTest(newDocLeaWong).setProps("date", "").toJson();
    private final String noDate = new RequestForTest(newDocLeaWong).setProps("date", null).toJson();
    private final String wrongDateFormat = new RequestForTest(newDocLeaWong).setProps("date", "2021-10-11 11:00").toJson();


    CheckResult testPostApi(String api, String body, int status, String message) {
        HttpResponse response = post(api, body).send();
        if (response.getStatusCode() != status) {
            return CheckResult.wrong("POST " + api + " should respond with "
                    + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                    + message + "\n"
                    + "Response body:\n" + response.getContent() + "\n"
                    + "Request body:\n" + body);
        }
        return CheckResult.correct();
    }

    CheckResult testGetApi(String api, int status, String message) {
        HttpResponse response = get(api).send();
        if (response.getStatusCode() != status) {
            return CheckResult.wrong("GET " + api + " should respond with "
                    + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                    + message + "\n"
                    + "Response body:\n" + response.getContent() + "\n"
            );
        }
        System.out.println(response.getContent());
        return CheckResult.correct();
    }

    @DynamicTest(order = 1)
    DynamicTesting[] dt = new DynamicTesting[]{

            // negative tests

            () -> testGetApi(appointments, 204, "Wrong Status code"),
            () -> testPostApi(setAppointment, doctorNameEmpty, 400, "Empty doctor field!"),
            () -> testPostApi(setAppointment, noDoctorName, 400, "doctor field is absent!"),
            () -> testPostApi(setAppointment, doctorNameSpaces, 400, "doctor field is absent!"),
            () -> testPostApi(setAppointment, patientNameEmpty, 400, "Empty patient field!"),//#5
            () -> testPostApi(setAppointment, noPatientName, 400, "patient field is absent!"),
            () -> testPostApi(setAppointment, patientSpaces, 400, "patient field is absent!"),

            () -> testPostApi(setAppointment, dateEmpty, 400, "Empty date field!"),
            () -> testPostApi(setAppointment, noDate, 400, "date field is absent!"),
            () -> testGetApi(appointments, 204, "Wrong Status code"),//#10
            () -> testPostApi(setAppointment, wrongDateFormat, 400, "wrong date format"),

            //SetAppointsCheck
            () -> testPostSetAppointments(leaWongApp1),//#12
            () -> testPostSetAppointments(leaWongApp2),//#13
            () -> testPostSetAppointments(leaWongApp3),//#14

            //GetAllAppointmentsCheck
            () -> testGetAllappointments(),//#15

            //SetAppointsCheck
            () -> testPostSetAppointments(pamelaUppersonApp1),//#16
            () -> testPostSetAppointments(pamelaUppersonApp2),//#17
            () -> testPostSetAppointments(pamelaUppersonApp3),//#18
            () -> testPostSetAppointments(leaWongApp4),//#19
            () -> testPostSetAppointments(pamelaUppersonApp4),//#20

            //GetAllAppointmentsCheck
            () -> testGetAllappointments(),//#21

            () -> testDeleteAppointment(),//#22

            () -> testDeleteAppointmentApi(400, "Wrong Status code"),
            () -> testGetApi(appointments, 204, "Wrong Status code"),//#23

            //deleteAppointemntsCheck
            () -> testDeleteAppointment(),//#24

            () -> testPostSetAppointments(pamelaUppersonApp1),//#25
            () -> testPostSetAppointments(pamelaUppersonApp2),//#26
            () -> testPostSetAppointments(pamelaUppersonApp3),//#27
            () -> testGetAllappointments(),//#28
            () -> testPostSetAppointments(leaWongApp1),//#29
            () -> testDeleteAppointment(),//#30
    };


    // Test Post setAppointment
    CheckResult testPostSetAppointments(String appBody) {
        HttpResponse response = post("/setAppointment", appBody).send();

        if (response.getStatusCode() != 200) {
            return CheckResult.wrong("POST /setAppointment should respond with " +
                    "status code 200, responded: " + response.getStatusCode() + "\n\n" +
                    "Response body:\n" + response.getContent()+ "\n"
                    + "Request body:\n" + appBody);
        }

        JsonElement json;
        try {
            json = response.getJson();
        } catch (Exception ex) {
            return CheckResult.wrong("POST /setAppointment should return a valid JSON");
        }

        JsonObject userJson = getJson(appBody).getAsJsonObject();
        String patient = userJson.get("patient").getAsString().trim().toLowerCase();
        String doctor = userJson.get("doctor").getAsString().toLowerCase().trim();
        String date = userJson.get("date").getAsString().toLowerCase().trim();
        System.out.println(date + "   " + doctor + " " + patient);

        expect(String.valueOf(json)).asJson().check(

                isObject()
                        .value("idApp", isNumber())
                        .value("doctor", doctor)
                        .value("patient", patient)
                        .value("date", date)

        );


        long id = getJson(response.getContent()).getAsJsonObject().get("idApp").getAsLong();
        if (idsForAppointments.contains(id)) {
            return CheckResult.wrong("id should be unique. This id " + id + " exist");
        }
        appointmentsCorrectJson.add(response.getContent());
        idsForAppointments.add(id);


        return CheckResult.correct();
    }

    CheckResult testGetAllappointments() {

        HttpResponse response = get("/appointments").send();

        if (response.getStatusCode() != 200) {
            return CheckResult.wrong("GET /appointments should respond with " +
                    "status code 200, responded: " + response.getStatusCode() + "\n\n" +
                    "Response body:\n" + response.getContent());
        }

        JsonElement json;
        try {
            json = response.getJson();
        } catch (Exception ex) {
            return CheckResult.wrong("GET /appointments should return a valid JSON");
        }

        if (!response.getJson().isJsonArray()) {
            return CheckResult.wrong("GET /appointments. Wrong object in response, expected array of JSON but was \n" +
                    response.getContent().getClass());
        }

        System.out.println(response.getContent());
        System.out.println(appointmentsCorrectJson.toString());

        String correctJsonToString = convert(appointmentsCorrectJson.toArray(new String[appointmentsCorrectJson.size()]));
        JsonArray correctJson = getJson(correctJsonToString).getAsJsonArray();
        JsonArray responseJson = getJson(response.getContent()).getAsJsonArray();


        if (responseJson.size() != correctJson.size()) {
            return CheckResult.wrong("Correct json array size should be" +
                    correctJson.size() + "\n\n" +
                    "Response array size is:\n" + responseJson.size());
        }


        for (int i = 0; i < responseJson.size(); i++) {


            expect(responseJson.get(i).getAsJsonObject().toString()).asJson()
                    .check(isObject()
                            .value("idApp", correctJson.get(i).getAsJsonObject().get("idApp").getAsLong())
                            .value("doctor", correctJson.get(i).getAsJsonObject().get("doctor").getAsString())
//                            .value("specialization", correctJson.get(i).getAsJsonObject().get("specialization").getAsString())
//                            .value("doctorId", correctJson.get(i).getAsJsonObject().get("doctorId").getAsLong())
                            .value("patient", correctJson.get(i).getAsJsonObject().get("patient").getAsString())
                            .value("date", correctJson.get(i).getAsJsonObject().get("date").getAsString()));
//            ids.add(correctJson.get(i).getAsJsonObject().get("idApp").getAsLong());

        }

        return CheckResult.correct();
    }

    CheckResult testDeleteAppointment() {
        int size = idsForAppointments.size();
        for (int i = 0; i < size; i++) {


            HttpResponse response = delete("deleteAppointment?id=" + idsForAppointments.get(i)).send();


            if (response.getStatusCode() == 409 && response.getContent().contains("The appointment does not exist or one of the fields is null!")) {
                System.out.println(response.getContent());
                return CheckResult.correct();
            } else if (response.getStatusCode() == 400 && response.getContent().contains("The appointment does not exist or was already cancelled")) {
                System.out.println(response.getContent());
                return CheckResult.correct();
            } else if (response.getStatusCode() == 500 && response.getContent().contains("The date field is null!")) {
                System.out.println(response.getContent());
                return CheckResult.correct();
            }

            if (response.getStatusCode() != 200) {
                return CheckResult.wrong("DELETE /deleteAppointment?id= should respond with " +
                        "status code 200, responded: " + response.getStatusCode() + "\n\n" +
                        "Response body:\n" + response.getContent());
            }

            JsonElement json;
            try {
                json = response.getJson();
            } catch (Exception ex) {
                return CheckResult.wrong("DELETE /deleteAppointment?id= should return a valid JSON");
            }



            String correctJsonToString = convert(appointmentsCorrectJson.toArray(new String[appointmentsCorrectJson.size()]));
            JsonArray correctJson = getJson(correctJsonToString).getAsJsonArray();
            JsonObject responseJson = null;
            try {
                responseJson = getJson(response.getContent()).getAsJsonObject();
            } catch (Exception e) {
                return CheckResult.wrong("DELETE /deleteAppointment?id= should return a valid JSON\n" + e.getMessage());
            }

            expect(responseJson.toString()).asJson()
                    .check(isObject()
                            .value("idApp", correctJson.get(i).getAsJsonObject().get("idApp").getAsLong())
                            .value("doctor", correctJson.get(i).getAsJsonObject().get("doctor").getAsString())
                            .value("patient", correctJson.get(i).getAsJsonObject().get("patient").getAsString())
                            .value("date", correctJson.get(i).getAsJsonObject().get("date").getAsString()));


        }

        for (int i = 0; i < size; i++) {

            appointmentsCorrectJson.remove(0);
            idsForAppointments.remove(0);
        }
        return CheckResult.correct();
    }

    CheckResult testDeleteAppointmentApi(int status, String message) {
        HttpResponse response = delete("deleteAppointment?id=" + 110).send();
        if (response.getStatusCode() != status) {
            return CheckResult.wrong("DELETE " + "deleteAppointment?id= 11" + " should respond with "
                    + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                    + message + "\n"
                    + "Response body:\n" + response.getContent() + "\n"
            );
        }

        if (response.getStatusCode() == 400 && !response.getContent().contains("The appointment does not exist or was already cancelled")) {
            return CheckResult.wrong("Expected  response : \"The appointment does not exist or was already cancelled\" but received " +
                    response.getContent());
        }


        return CheckResult.correct();
    }
}
