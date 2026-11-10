"""M2: reminders 增 sent_at / read_at（红点）

Revision ID: a1f2c3d4e5f6
Revises: a3f8c2d91e04
Create Date: 2026-11-10

"""
from typing import Sequence, Union

import sqlalchemy as sa
from alembic import op

# revision identifiers, used by Alembic.
revision: str = 'a1f2c3d4e5f6'
down_revision: Union[str, Sequence[str], None] = 'a3f8c2d91e04'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """reminders 表增 sent_at / read_at 列。"""
    op.add_column('reminders', sa.Column('sent_at', sa.DateTime(), nullable=True))
    op.add_column('reminders', sa.Column('read_at', sa.DateTime(), nullable=True))


def downgrade() -> None:
    """回滚：删 sent_at / read_at 列。"""
    op.drop_column('reminders', 'read_at')
    op.drop_column('reminders', 'sent_at')
