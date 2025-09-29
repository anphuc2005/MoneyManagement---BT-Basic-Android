package com.example.moneymanagement.data.model

class TransactionSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double
) {
    fun getDefaultCategories(): List<Category> {
        return listOf(
            Category(1, "Lương", "salary_icon", TransactionType.INCOME),
            Category(2, "Thưởng", "bonus_icon", TransactionType.INCOME),
            Category(3, "Đầu tư", "investment_icon", TransactionType.INCOME),
            Category(4, "Khác", "other_income_icon", TransactionType.INCOME),


            Category(5, "Thực phẩm", "food_icon", TransactionType.EXPENSE),
            Category(6, "Cà phê", "coffee_icon", TransactionType.EXPENSE),
            Category(7, "Xăng xe", "gas_icon", TransactionType.EXPENSE),
            Category(8, "Thời trang", "fashion_icon", TransactionType.EXPENSE),
            Category(9, "Giải trí", "entertainment_icon", TransactionType.EXPENSE),
            Category(10, "Thú cưng", "pet_icon", TransactionType.EXPENSE),
            Category(11, "Giáo dục", "education_icon", TransactionType.EXPENSE),
            Category(12, "Y tế", "medical_icon", TransactionType.EXPENSE),
            Category(13, "Du lịch", "travel_icon", TransactionType.EXPENSE),
            Category(14, "Hoá đơn tiền", "bill_icon", TransactionType.EXPENSE),
            Category(15, "Quà tặng", "gift_icon", TransactionType.EXPENSE)
        )
    }
}



