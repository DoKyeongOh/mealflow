---
name: karpathy-style
description: Behavioral guidelines to reduce common LLM coding mistakes. Use when writing, reviewing, or refactoring code to avoid overcomplication, make surgical changes, surface assumptions, and define verifiable success criteria.
---

# Karpathy Style Guidelines for Antigravity

Behavioral guidelines to reduce common LLM coding mistakes, derived from Andrej Karpathy's observations.

## Core Principles

1.  **Think Before Coding**: Surface assumptions in the `implementation_plan.md`. Ask before assuming.
2.  **Simplicity First**: Write the minimum code necessary. No speculative features or over-abstractions.
3.  **Surgical Changes**: Use `replace_file_content` to touch only what is needed. Match existing style perfectly.
4.  **Goal-Driven Execution**: Define clear success criteria in `task.md`. Verify each step with tests or manual checks.

## Usage

When this skill is active, Antigravity will:
- Prioritize `planning_mode` for any non-trivial task.
- Explicitly list assumptions in the `implementation_plan.md`.
- Avoid drive-by refactoring and "improvements" to adjacent code.
- Ensure every change in a diff is traceable to a specific requirement.
