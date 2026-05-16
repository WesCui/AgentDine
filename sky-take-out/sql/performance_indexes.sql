-- Sky Takeout 高频查询索引优化脚本
-- 使用前请先在目标数据库中确认现有索引，避免重复创建。
-- 该脚本基于当前仓库中的 Mapper / XML 查询条件整理，重点覆盖订单分页、超时扫描、购物车查重、地址默认查询等场景。

-- 员工登录
ALTER TABLE employee
    ADD INDEX idx_employee_username (username);

-- 微信用户登录
ALTER TABLE user
    ADD INDEX idx_user_openid (openid);

-- 订单支付回调按订单号查询
ALTER TABLE orders
    ADD INDEX idx_orders_number (number);

-- 用户端历史订单、管理端订单筛选、按状态统计
ALTER TABLE orders
    ADD INDEX idx_orders_user_status_time (user_id, status, order_time),
    ADD INDEX idx_orders_status_time (status, order_time);

-- 订单明细按订单 ID 查询
ALTER TABLE order_detail
    ADD INDEX idx_order_detail_order_id (order_id);

-- 菜品、套餐按分类和状态查询
ALTER TABLE dish
    ADD INDEX idx_dish_category_status (category_id, status);

ALTER TABLE setmeal
    ADD INDEX idx_setmeal_category_status (category_id, status);

-- 地址簿默认地址查询
ALTER TABLE address_book
    ADD INDEX idx_address_book_user_default (user_id, is_default);

-- 购物车查重与列表查询
ALTER TABLE shopping_cart
    ADD INDEX idx_shopping_cart_user_dish_setmeal_flavor (user_id, dish_id, setmeal_id, dish_flavor);

-- 套餐与菜品关系查询
ALTER TABLE setmeal_dish
    ADD INDEX idx_setmeal_dish_setmeal_id (setmeal_id),
    ADD INDEX idx_setmeal_dish_dish_id (dish_id);
