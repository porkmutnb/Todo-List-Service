# ToDoList Project (Full-Stack Application)

ยินดีต้อนรับสู่โปรเจกต์ **ToDoList** แอปพลิเคชัน Full-Stack สำหรับจัดการรายการงาน โครงการ และหมวดหมู่ต่าง ๆ โปรเจกต์นี้ได้รับการพัฒนาแยกเป็นฝั่งระบบหลังบ้าน (Spring Boot) และหน้าบ้าน (Angular) พร้อมใช้งานร่วมกับ PostgreSQL Database

---

## 🛠️ เครื่องมือที่ต้องเตรียมก่อนเริ่มใช้งาน (Prerequisites)
เพื่อให้สามารถติดตั้งและรันโปรเจกต์ได้สำเร็จ กรุณาติดตั้งเครื่องมือเหล่านี้บนเครื่องของคุณ:

* **Docker Desktop / Docker Engine** (แนะนำสำหรับการรันทุกระบบแบบไร้รอยต่อ)
* **Node.js** (เวอร์ชัน 18+ หรือ 20 LTS ขึ้นไป) และ **npm** (สำหรับฝั่ง Frontend)
* **JDK 21 หรือ JDK 25** (สำหรับฝั่ง Backend แบบ Standalone)
* **Bruno API Client** (สำหรับทดสอบ API จากโฟลเดอร์ `bruno-api`)

---

## 🚀 วิธีที่ 1: รันด่วนผ่าน Docker Compose (แนะนำที่สุด ทำตามได้ทันที)
วิธีนี้จะติดตั้งและรันทุกบริการ (Database, Backend, Frontend) ให้ทำงานร่วมกันบน Docker Container โดยอัตโนมัติ

### ขั้นตอนการเริ่มใช้งาน:
1. **เปิด Docker Desktop** ในเครื่องของคุณให้พร้อมทำงาน
2. **รันคำสั่งด้านล่างนี้** ที่โฟลเดอร์ระดับนอกสุดของโปรเจกต์:
   ```bash
   docker compose up -d --build
   ```
3. **ตรวจสอบว่าทุกบริการรันสำเร็จ:**
   ```bash
   docker compose ps
   ```
   *(คุณควรเห็นคอนเทนเนอร์ `todolist-postgres`, `todolist-backend`, และ `todolist-frontend` กำลังทำงานอยู่)*

### การเข้าใช้งานระบบ:
* 🌐 **เว็บแอปพลิเคชัน (Frontend):** [http://localhost:4200](http://localhost:4200)
* ⚙️ **บริการหลังบ้าน (Backend API):** [http://localhost:8080](http://localhost:8080)
* 🗄️ **ฐานข้อมูล (PostgreSQL):** `localhost:5432` (Username: `chermew` / Password: `P@ssw0rd`)

---

## 💻 วิธีที่ 2: ตั้งค่าและรันแบบทีละส่วนงาน (Standalone / Development Mode)
เหมาะสำหรับนักพัฒนาที่ต้องการแก้ไขโค้ดและรันแบบแยกฝั่ง

### ขั้นตอนที่ 1: การเตรียมและรันฐานข้อมูล
1. รัน PostgreSQL บนเครื่องของคุณที่พอร์ต `5432`
2. สร้างฐานข้อมูลใหม่ชื่อ `todolist`:
   ```sql
   CREATE DATABASE todolist;
   ```
   *(หรือสั่งรันเฉพาะ Database ผ่าน Docker ด่วนด้วยคำสั่ง `docker compose up db -d`)*

### ขั้นตอนที่ 2: ตั้งค่าและรันระบบหลังบ้าน (Backend)
1. เปิด Terminal แล้วเข้าไปยังโฟลเดอร์ `backend`:
   ```bash
   cd backend
   ```
2. แก้ไขข้อมูลการเชื่อมต่อฐานข้อมูลในไฟล์ [application.yaml](file:///d:/cherMew/ToDoList/backend/src/main/resources/application.yaml) ให้ตรงกับเครื่องของคุณ (หากรันผ่าน Database ของ Docker อยู่แล้วสามารถข้ามขั้นตอนนี้ได้)
3. สั่งรันแอปพลิเคชัน:
   * **สำหรับ Windows:** `.\mvnw.cmd spring-boot:run`
   * **สำหรับ Linux/macOS:** `chmod +x mvnw && ./mvnw spring-boot:run`

### ขั้นตอนที่ 3: ตั้งค่าและรันระบบหน้าบ้าน (Frontend)
1. เปิด Terminal ใหม่แยกอีกหน้าต่าง แล้วเข้าไปยังโฟลเดอร์ `frontend`:
   ```bash
   cd frontend
   ```
2. ติดตั้ง Dependencies และรันบริการ:
   ```bash
   npm install
   npm run start
   ```
3. เปิดเว็บเบราว์เซอร์เข้าใช้งานที่: [http://localhost:4200](http://localhost:4200)

---

## 🧪 การทดสอบ REST API ด้วย Bruno API Client
ที่ระดับนอกสุดของโปรเจกต์จะมีโฟลเดอร์ **[bruno-api](file:///d:/cherMew/ToDoList/bruno-api)** ซึ่งบรรจุ Collection สำหรับการยิงทดสอบ API ไว้ทั้งหมด

### วิธีการใช้งาน:
1. ดาวน์โหลดและเปิดโปรแกรม **Bruno**
2. คลิกปุ่ม **"Open Collection"** ในหน้าแรก
3. เลือกโฟลเดอร์ **[bruno-api](file:///d:/cherMew/ToDoList/bruno-api)** ที่อยู่ในโฟลเดอร์โปรเจกต์นี้ แล้วกดตกลง
4. คุณจะพบกับ Endpoint ทั้งหมด (Auth, Users, Projects, Categories, Todos) พร้อมทดสอบยิงได้ทันที

---

## 🛑 การหยุดทำงานและลบระบบ (Stop & Clean)
หากรันระบบผ่าน Docker Compose และต้องการปิดการทำงานพร้อมลบคอนเทนเนอร์ทั้งหมด ให้รันคำสั่ง:
```bash
docker compose down
```
*(หมายเหตุ: ข้อมูลในฐานข้อมูลจะไม่สูญหาย เนื่องจากมีการจัดเก็บข้อมูลไว้ใน Docker Volume `postgres_data` อย่างปลอดภัย)*
