# Examples (Antigravity Style)

Real-world code examples demonstrating the four principles. Each example shows what LLMs commonly do wrong and how to fix it in Antigravity.

---

## 1. Think Before Coding

### Example 1: Hidden Assumptions

**User Request:** "Add a feature to export user data"

**❌ What LLMs Do (Wrong Assumptions)**
Immediately writes code to export ALL users to `users.json` without asking about format, volume, or destination.

**✅ What Should Happen (Surface Assumptions in Implementation Plan)**
```markdown
## Open Questions
1. **Scope**: Export all users or filtered subset?
2. **Format**: JSON, CSV, or something else?
3. **Destination**: File download, email, or API response?
```

---

## 2. Simplicity First

### Example 1: Over-abstraction

**User Request:** "Add a function to calculate discount"

**❌ What LLMs Do (Overengineered)**
Creates `DiscountStrategy`, `PercentageDiscount`, `FixedDiscount`, `DiscountCalculator` classes.

**✅ What Should Happen (Simple)**
```java
public double calculateDiscount(double amount, double percent) {
    return amount * (percent / 100);
}
```

---

## 3. Surgical Changes

### Example 1: Drive-by Refactoring

**User Request:** "Fix the bug where empty emails crash the validator"

**❌ What LLMs Do (Too Much)**
Replaces the whole file, changes quote styles, adds type hints, and "improves" unrelated validation logic.

**✅ What Should Happen (Surgical with `replace_file_content`)**
```diff
- if (email == null) {
+ if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email required");
  }
```

---

## 4. Goal-Driven Execution

### Example 1: Vague vs. Verifiable

**User Request:** "Fix the authentication system"

**❌ What LLMs Do (Vague Approach)**
"I'll review the code, find bugs, and fix them."

**✅ What Should Happen (Verifiable Goals in `task.md`)**
```markdown
- [ ] Create reproduction test case for session invalidation bug
- [ ] Implement session invalidation on password change
- [ ] Verify fix with test case
- [ ] Run full auth test suite
```

---

**Good code is code that solves today's problem simply, not tomorrow's problem prematurely.**
