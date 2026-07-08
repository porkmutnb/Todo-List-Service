-- ==========================================
-- 1. ตารางผู้ใช้งาน (USERS TABLE)
-- ==========================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'active', -- active, inactive
    bio VARCHAR(255),
    avatar_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- ==========================================
-- 2. ตารางโปรเจกต์ (PROJECTS TABLE)
-- ==========================================
CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- ==========================================
-- 3. ตารางสมาชิกทีม (PROJECT MEMBERS TABLE)
-- สิทธิ์ระหว่าง Owner และ Member จะแยกด้วยตารางความสัมพันธ์นี้
-- ==========================================
CREATE TABLE IF NOT EXISTS project_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'member', -- owner, member (owner สามารถเตะ member ได้)
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, user_id) -- ป้องกัน user ซ้ำในโปรเจกต์เดิม
);
-- ==========================================
-- 4. ตารางหมวดหมู่ (CATEGORIES TABLE)
-- ==========================================
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    color_code VARCHAR(7), -- เช่น #FF5733 สำหรับแสดงสีหมวดหมู่ใน UI
    assigned_to UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, name) -- ป้องกันชื่อหมวดหมู่ซ้ำกันในโปรเจกต์เดียวกัน
);
-- ==========================================
-- 5. ตารางงาน (TODOS TABLE)
-- ==========================================
CREATE TABLE IF NOT EXISTS todos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL, -- ถ้าลบหมวดหมู่ ให้งานยังอยู่แต่ไม่มีหมวดหมู่
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, in_progress, completed
    priority VARCHAR(20) NOT NULL DEFAULT 'medium', -- low, medium, high
    due_date TIMESTAMP WITH TIME ZONE,
    assigned_to UUID REFERENCES users(id) ON DELETE SET NULL, -- มอบหมายงานให้ลูกทีมคนไหน
    created_by UUID REFERENCES users(id) ON DELETE SET NULL, -- ใครเป็นคนสร้างงานนี้
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- ==========================================
-- 6. ตารางบันทึกกิจกรรม (LOGGER TABLE)
-- ==========================================
CREATE TABLE IF NOT EXISTS logger (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL, -- ใครทำ (หากลบ user ให้เปลี่ยนเป็น null)
    action VARCHAR(50) NOT NULL, -- เช่น CREATE_TODO, DELETE_MEMBER, UPDATE_STATUS
    entity_type VARCHAR(50) NOT NULL, -- ทำกับตารางไหน เช่น 'todos', 'project_members'
    entity_id UUID, -- ID ของแถวข้อมูลที่โดนกระทำ เช่น ID ของ Todo ที่โดนลบ
    path VARCHAR(255),
    request_body TEXT,
    response_body TEXT,
    http_status INT,
    affected_tables TEXT, -- รายการการทำงานหลายตารางเรียงลำดับกันในรูป JSON string
    details TEXT, -- รายละเอียดเพิ่มเติม เช่น "เปลี่ยนสถานะจาก pending เป็น completed"
    ip_address VARCHAR(45), -- รองรับทั้ง IPv4 และ IPv6
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- ==========================================
-- 7. การสร้าง INDEX เพื่อเพิ่มความเร็วในการสืบค้นข้อมูล (Performance Optimization)
-- ==========================================
CREATE INDEX IF NOT EXISTS idx_project_members_user ON project_members(user_id);
CREATE INDEX IF NOT EXISTS idx_todos_project ON todos(project_id);
CREATE INDEX IF NOT EXISTS idx_todos_category ON todos(category_id);
CREATE INDEX IF NOT EXISTS idx_logger_user ON logger(user_id);