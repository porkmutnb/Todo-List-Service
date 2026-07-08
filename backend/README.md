# ToDoList Backend Service

นี่คือส่วนบริการหลังบ้าน (Backend) ของระบบจัดการรายการงาน (ToDoList) ซึ่งพัฒนาด้วย Spring Boot (Java) เพื่อให้บริการ RESTful API แก่ส่วนหน้าบ้าน (Frontend) ในการจัดการงาน, โครงการ, หมวดหมู่ และระบบสมาชิก

---

## 1. Tech Stack ที่ใช้
* **Language:** Java 25 (รองรับการรันร่วมกับ JDK 21+)
* **Framework:** Spring Boot 4.1.0 (Spring WebMVC)
* **Security:** Spring Security & JSON Web Tokens (JWT) สำหรับระบุและยืนยันตัวตน
* **Database Access:** Spring Data JPA (Hibernate)
* **Database:** PostgreSQL
* **Utilities:** Lombok (สำหรับลด Boilerplate Code)
* **Testing:** JUnit 5, Mockito, JaCoCo (สำหรับวัด Test Coverage)

---

## 2. โครงสร้างบริการหลัก (Core Services)
ระบบถูกแบ่งการทำงานออกเป็น Service ย่อยตามหน้าที่หลักดังนี้:

* **AuthService:** จัดการการลงทะเบียนผู้ใช้งานใหม่ (`register`) และการเข้าสู่ระบบ (`login`)
* **UserService:** จัดการข้อมูลและสิทธิ์ของผู้ใช้งานในระบบ รวมถึงดึงโปรไฟล์ส่วนตัว
* **ProjectService:** จัดการโปรเจกต์งานกลุ่มหรือส่วนตัว สามารถสร้าง, แก้ไข, ลบ และจัดการสมาชิกในโครงการได้
* **CategoryService:** จัดการหมวดหมู่ (Category) ในแต่ละโครงการ เพื่อจัดกลุ่มงานให้ชัดเจน
* **TodoService:** จัดการตัวงานหลัก (ToDo Items) เช่น การเพิ่มงาน, ลบงาน, กำหนดวันส่ง (Due Date), ลำดับความสำคัญ (Priority) และสถานะงาน

---

## 3. เส้นทางบริการ API ทั้งหมด (REST API Endpoints)

ทุก API Endpoint จะทำงานผ่าน URL พื้นฐาน: `http://localhost:8080/api/v1`

### 1. ระบบยืนยันตัวตน (Authentication)
*ไม่ต้องแนบ Authorization Token ใน Header*
* **`POST /api/v1/register`** - สมัครสมาชิกผู้ใช้งานใหม่
* **`POST /api/v1/login`** - เข้าสู่ระบบ (จะได้รับ JWT Token สำหรับเข้าใช้ API อื่นๆ)

### 2. ระบบจัดการผู้ใช้ (User Management)
*ต้องการ Header `Authorization: Bearer <JWT_TOKEN>`*
* **`GET /api/v1/users/profile`** - ดึงข้อมูลโปรไฟล์ของตนเองที่ล็อกอินอยู่
* **`GET /api/v1/users/{userId}`** - ดึงข้อมูลผู้ใช้อื่นด้วย User ID
* **`PUT /api/v1/users/profile`** - อัปเดตข้อมูลโปรไฟล์ของตนเอง

### 3. ระบบจัดการโครงการ (Project Management)
*ต้องการ Header `Authorization: Bearer <JWT_TOKEN>`*
* **`GET /api/v1/projects`** - ดึงรายการโครงการทั้งหมดที่เป็นเจ้าของหรือสมาชิก
* **`POST /api/v1/projects`** - สร้างโครงการใหม่
* **`GET /api/v1/projects/{projectId}`** - ดึงข้อมูลโครงการตาม ID
* **`PUT /api/v1/projects/{projectId}`** - อัปเดตข้อมูลโครงการ
* **`DELETE /api/v1/projects/{projectId}`** - ลบโครงการ
* **`GET /api/v1/projects/{projectId}/categories`** - ดึงรายการหมวดหมู่และงานทั้งหมดภายใต้โครงการที่เลือก

### 4. ระบบจัดการหมวดหมู่ (Category Management)
*ต้องการ Header `Authorization: Bearer <JWT_TOKEN>`*
* **`POST /api/v1/categories`** - สร้างหมวดหมู่ใหม่
* **`GET /api/v1/categories/{categoryId}`** - ดึงข้อมูลหมวดหมู่ตาม ID
* **`PUT /api/v1/categories/{categoryId}`** - อัปเดตข้อมูลหมวดหมู่
* **`DELETE /api/v1/categories/{categoryId}`** - ลบหมวดหมู่

### 5. ระบบจัดการรายการงาน (ToDo Management)
*ต้องการ Header `Authorization: Bearer <JWT_TOKEN>`*
* **`POST /api/v1/todos`** - สร้างงานใหม่
* **`GET /api/v1/todos/{todoId}`** - ดึงข้อมูลงานตาม ID
* **`PUT /api/v1/todos/{todoId}`** - อัปเดตข้อมูลงาน (เช่น ชื่อ, ลำดับความสำคัญ, สถานะการทำงาน)
* **`DELETE /api/v1/todos/{todoId}`** - ลบงาน
* **`GET /api/v1/todos/{categoryId}/todos`** - ดึงข้อมูลงานทั้งหมดที่อยู่ในหมวดหมู่ที่เลือก

---

## 4. วิธีการนำเข้า REST API Collection ด้วยโปรแกรม Bruno

โปรเจกต์นี้มีไฟล์ API Collection ที่บันทึกไว้ในรูปแบบของโปรแกรม **Bruno** (Lightweight API Client) อยู่ในโฟลเดอร์ **[bruno-api](file:///d:/cherMew/ToDoList/bruno-api)** ที่อยู่ในระดับ Root ของโปรเจกต์

คุณสามารถนำข้อมูลเข้าสู่โปรแกรม Bruno เพื่อทำการกดทดสอบ API ทุกเส้นได้ดังนี้:

1. **ดาวน์โหลดและติดตั้ง Bruno:** เข้าเว็บไซต์ [usebruno.com](https://www.usebruno.com/) แล้วเลือกเวอร์ชันติดตั้งตาม OS ของคุณ
2. **เปิด Collection:**
   - เปิดโปรแกรม Bruno
   - คลิกที่ปุ่ม **"Open Collection"** ในหน้าแรกของโปรแกรม
3. **เลือกตำแหน่งโฟลเดอร์:**
   - ค้นหาและเลือกโฟลเดอร์ **[bruno-api](file:///d:/cherMew/ToDoList/bruno-api)** ในเครื่องของคุณ
   - กดปุ่ม **Open** หรือ **Select Folder**
4. **เริ่มต้นทดสอบ:**
   - คุณจะเห็นรายการโฟลเดอร์ย่อยแบ่งเป็นกลุ่ม API เช่น `Auth`, `User`, `Projects`, `Categories`, `Todos` ปรากฏทางแถบด้านซ้ายมือ
   - **การยืนยันตัวตน:** มีการตั้งค่าตัวแปรสภาพแวดล้อม (Environment) ไว้รองรับระบบ สามารถเปิดใช้งานและกรอก token เพื่อนำไปใช้ยิงทดสอบ API เส้นอื่นได้อย่างราบรื่น

---

## 5. ขั้นตอนการเตรียมระบบและรันแอปพลิเคชันแบบ Standalone
หากต้องการรันเฉพาะส่วน Backend บนเครื่องโดยไม่ผ่าน Docker Compose ให้ทำตามขั้นตอนดังต่อไปนี้:

### สิ่งที่ต้องติดตั้งบนเครื่องก่อน (Prerequisites)
1. **JDK 21 หรือ JDK 25** (แนะนำให้ติดตั้งตัวล่าสุด)
2. **PostgreSQL Server** รันอยู่ที่เครื่อง (Local) หรือ Server ที่ต้องการ

### ขั้นตอนที่ 1: เตรียมฐานข้อมูล (Database Preparation)
1. เข้าไปยัง PostgreSQL จากนั้นทำสร้างฐานข้อมูลใหม่ชื่อ `todolist`:
   ```sql
   CREATE DATABASE todolist;
   ```
2. สร้าง User และ Password ให้ตรงกับค่าในคอนฟิก หรือใช้ค่าเดิมที่มีอยู่

### ขั้นตอนที่ 2: แก้ไขการตั้งค่าฐานข้อมูล
เปิดไฟล์ [application.yaml](file:///d:/cherMew/ToDoList/backend/src/main/resources/application.yaml) แล้วอัปเดตข้อมูลการเชื่อมต่อดังนี้:
```yaml
spring:
  application:
    name: backend
  datasource:
    url: jdbc:postgresql://localhost:5432/todolist
    username: <ชื่อผู้ใช้_postgres_ของคุณ>
    password: <รหัสผ่าน_postgres_ของคุณ>
    driver-class-name: org.postgresql.Driver
```

### ขั้นตอนที่ 3: คอมไพล์และรันแอปพลิเคชัน
เปิด Terminal ในโฟลเดอร์ `backend` แล้วรันคำสั่งดังต่อไปนี้:

* **สำหรับระบบปฏิบัติการ Linux/macOS:**
  ```bash
  chmod +x mvnw
  ./mvnw spring-boot:run
  ```

* **สำหรับระบบปฏิบัติการ Windows (PowerShell/CMD):**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```

เมื่อรันสำเร็จ ตัวระบบหลังบ้านจะทำงานที่พอร์ต `8080` (สามารถทดสอบเรียกใช้งานได้ที่ `http://localhost:8080`)

---

## 6. ผลการรัน Unit Test ของระบบย่อย (Unit Test Results)

คุณสามารถตรวจสอบความถูกต้องของระบบและรัน Unit Test ทั้งหมดด้วยคำสั่ง:
```bash
# บน Windows
.\mvnw.cmd test

# บน macOS/Linux
./mvnw test
```

ผลการทดสอบทั้งหมดผ่านการรันสำเร็จ 100% (รวม 49 เคส)
```text
[INFO] Results:
[INFO] 
[INFO] Tests run: 49, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS (Tests execute and pass 100%)
[INFO] ------------------------------------------------------------------------
```
*ปลั๊กอิน JaCoCo (เวอร์ชัน 0.8.12) ในโปรเจกต์อาจแสดงข้อความแจ้งเตือน (Warning) เรื่อง bytecode ของ Java 25 แต่ไม่มีผลต่อการทำงานและการผ่านการทดสอบหลัก*
